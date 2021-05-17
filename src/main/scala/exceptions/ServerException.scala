package exceptions

final case class InvalidMessage(message: String) extends Exception("Invalid message: " + message)