import openApi.SandboxMoneyAmount
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition

object ReplyDataHandler {
  def balance(currencies: Seq[CurrencyPosition]): String = {
    currencies
      .foldLeft(""){case (str, curr) => str + curr.getCurrency.toString + " " + curr.getBalance.longValueExact() + "\n"}
  }

  def tickerPrice(ticker: String, money: SandboxMoneyAmount): String = {
    ticker + "\n" + money.value.toString + money.currency.toString
  }
}
