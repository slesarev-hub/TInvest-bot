package openApi

import exceptions.{InvalidMoneyAmount, NegativeMoneyAmount}
import ru.tinkoff.invest.openapi.model.rest.{MoneyAmount, SandboxCurrency}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class SandboxMoneyAmount(currency: SandboxCurrency, value: BigDecimal){
  if (value < 0) {
    throw new NegativeMoneyAmount
  }
}

object SandboxMoneyAmount {
  implicit val ec = ExecutionContext.global

  def fromMoneyAmount(moneyAmount: MoneyAmount): Future[SandboxMoneyAmount] = Future{
    SandboxMoneyAmount(CurrencyConverter.fromCurrency(moneyAmount.getCurrency), moneyAmount.getValue)
  }

  def fromString(string: String): Future[Try[SandboxMoneyAmount]] = Future{
    val currencyBegin = string.length - 3
    val value = Try(BigDecimal(string.substring(0, currencyBegin)))
    val currency = SandboxCurrency.fromValue(string.substring(currencyBegin))
    (value, currency) match {
      case (Success(v), c) if c != null => Success(SandboxMoneyAmount(c, v))
      case _ => Failure(new InvalidMoneyAmount)
    }
  }

}
