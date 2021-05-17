import ru.tinkoff.invest.openapi.model.rest.{CurrencyPosition, SandboxRegisterRequest, SandboxSetCurrencyBalanceRequest}
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi
import sandbox.{CurrencyConverter, SandboxMoneyAmount}

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{ExecutionContext, Future}

class InvestApi {
  private val api = new OkHttpOpenApi(Env.getInvestToken, true)
  private val account = api.getSandboxContext.performRegistration(new SandboxRegisterRequest).join
  implicit val ec = ExecutionContext.global

  def setBalance(sandboxMoneyAmount: SandboxMoneyAmount): Future[Void] = {
    val request = new SandboxSetCurrencyBalanceRequest
    request.setCurrency(sandboxMoneyAmount.currency)
    request.setBalance(sandboxMoneyAmount.value.bigDecimal)
    toScala(api.getSandboxContext.setCurrencyBalance(request, account.getBrokerAccountId))
  }

  def getBalance: Future[Seq[CurrencyPosition]] = toScala(
    api
      .getPortfolioContext
      .getPortfolioCurrencies(account.getBrokerAccountId())
  ).map(_.getCurrencies.asScala.toSeq)

  def insertMoney(sandboxMoneyAmount: SandboxMoneyAmount): Future[Void] = {
    getBalance
      .flatMap(currencies => currencies
        .filter(position => CurrencyConverter.fromCurrency(position.getCurrency) == sandboxMoneyAmount.currency)
        .map(position => setBalance(sandboxMoneyAmount.copy(value = sandboxMoneyAmount.value + position.getBalance)))
        .head
      )
  }

}
