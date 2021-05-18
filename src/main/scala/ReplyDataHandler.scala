import openApi.SandboxMoneyAmount
import ru.tinkoff.invest.openapi.model.rest.{CurrencyPosition, InstrumentType, PlacedMarketOrder, Portfolio, PortfolioPosition}

object ReplyDataHandler {
  def balance(currencies: Seq[CurrencyPosition]): String = {
    currencies
      .foldLeft(""){case (str, curr) => str + curr.getCurrency.toString + " " + curr.getBalance.longValueExact() + "\n"}
  }

  def tickerPrice(ticker: String, money: SandboxMoneyAmount): String = {
    ticker + "\n" + money.value.toString + money.currency.toString
  }

  def portfolio(portfolio: Seq[PortfolioPosition]): String = {
    portfolio
      .filter(_.getInstrumentType == InstrumentType.STOCK)
      .foldLeft("Stocks\n"){case (str, p) => str + p.getTicker + " " + p.getLots + "\n"}
  }

  def stockOperation(ticker: String, order: PlacedMarketOrder) = {
    "Order: " + ticker.toUpperCase + " " + order.getRequestedLots + "\nStatus: " + order.getStatus
  }
}
