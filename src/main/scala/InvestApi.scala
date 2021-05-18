import exceptions.{NoCurrencyPosition, NotEnoughMoney, WrongStockTicker}
import openApi.{CurrencyConverter, SandboxMoneyAmount}
import ru.tinkoff.invest.openapi.model.rest._
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{ExecutionContext, Future}

class InvestApi {
  private val api = new OkHttpOpenApi(Env.getInvestToken, true)
  private val account = api.getSandboxContext.performRegistration(new SandboxRegisterRequest).join
  private implicit val ec = ExecutionContext.global

  private def setBalance(sandboxMoneyAmount: SandboxMoneyAmount): Future[Void] = {
    val request = new SandboxSetCurrencyBalanceRequest
    request.setCurrency(sandboxMoneyAmount.currency)
    request.setBalance(sandboxMoneyAmount.value.bigDecimal)
    toScala(api.getSandboxContext.setCurrencyBalance(request, account.getBrokerAccountId))
  }

  private def getMoneyPosition(sandboxMoneyAmount: SandboxMoneyAmount): Future[CurrencyPosition] = {
    getBalance.flatMap(_
      .filter(position => CurrencyConverter.fromCurrency(position.getCurrency) == sandboxMoneyAmount.currency)
      match {
        case Seq(position) => Future(position)
        case _ => Future.failed(new NoCurrencyPosition)
      }
    )
  }

  def getBalance: Future[Seq[CurrencyPosition]] = toScala(
    api.getPortfolioContext.getPortfolioCurrencies(account.getBrokerAccountId())
  ).map(_.getCurrencies.asScala.toSeq)

  def getStock(ticker: String): Future[MarketInstrument] =
    toScala(api.getMarketContext.searchMarketInstrumentsByTicker(ticker.toUpperCase))
      .map(_.getInstruments.asScala
        .filter(_.getType == InstrumentType.STOCK)
      )
      .flatMap{
        case Seq(hd) => Future(hd)
        case _ => Future.failed(WrongStockTicker(ticker))
      }

  def getPrice(ticker: String): Future[SandboxMoneyAmount] = {
    getStock(ticker)
      .flatMap { stock =>
        toScala(api.getMarketContext.getMarketOrderbook(stock.getFigi, 1))
          .flatMap { opt => if (opt.isPresent) Future(opt.get()) else Future.failed(new WrongStockTicker(ticker)) }
          .map(orderBook => SandboxMoneyAmount(CurrencyConverter.fromCurrency(stock.getCurrency), BigDecimal(orderBook.getLastPrice)))
      }
  }

  def getPortfolio(): Future[Seq[PortfolioPosition]] = toScala(
    api.getPortfolioContext.getPortfolio(account.getBrokerAccountId)
  ).map(_.getPositions.asScala.toSeq)

  def insertMoney(sandboxMoneyAmount: SandboxMoneyAmount): Future[Void] = {
    getMoneyPosition(sandboxMoneyAmount)
      .flatMap(position => setBalance(sandboxMoneyAmount.copy(value = sandboxMoneyAmount.value + position.getBalance)))
  }

  def withdrawMoney(sandboxMoneyAmount: SandboxMoneyAmount): Future[Void] = {
    getMoneyPosition(sandboxMoneyAmount)
      .flatMap(position =>
        (BigDecimal(position.getBalance) < sandboxMoneyAmount.value) match {
          case false => setBalance(sandboxMoneyAmount.copy(value = BigDecimal(position.getBalance) - sandboxMoneyAmount.value))
          case true => Future.failed(new NotEnoughMoney)
        }
      )
  }

  def placeMarketOrder(operation: String, ticker: String, lots: Int): Future[PlacedMarketOrder] = {
    val request = new MarketOrderRequest()
    request.setOperation(OperationType.fromValue(operation))
    request.setLots(lots)
    getStock(ticker)
      .flatMap(stock => toScala(api.getOrdersContext.placeMarketOrder(stock.getFigi, request, account.getBrokerAccountId)))
  }
}
