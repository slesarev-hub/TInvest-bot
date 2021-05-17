package sandbox

import ru.tinkoff.invest.openapi.model.rest.{Currency, SandboxCurrency}

object CurrencyConverter {
  def fromCurrency(currency: Currency): SandboxCurrency = SandboxCurrency.fromValue(currency.toString)
}
