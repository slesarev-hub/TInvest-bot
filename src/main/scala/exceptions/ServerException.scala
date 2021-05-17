package exceptions

final case class InvalidMessage(message: String) extends Exception("Invalid message: " + message)

final case class NoCurrencyPosition() extends Exception("No such currency position on balance")

final case class NegativeMoneyAmount() extends Exception("Negative money amount")

final case class NotEnoughMoney() extends Exception("Not enough money")