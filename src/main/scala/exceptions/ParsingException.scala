package exceptions

final case class InvalidMoneyAmount() extends Exception("Invalid parameter")

final case class WrongStockTicker(ticker: String) extends Exception("Wrong stock ticker " + ticker)