package sandbox

import ru.tinkoff.invest.openapi.model.rest.{Currency, SandboxCurrency}

object CurrencyConverter {
  def fromCurrency(currency: Currency) = SandboxCurrency.fromValue(currency.toString)
}
