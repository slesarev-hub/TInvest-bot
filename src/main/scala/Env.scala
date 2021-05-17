object Env {
  private def get : ujson.Value = ujson.read(os.read(os.pwd/"env.json"))
  def getInvestToken : String = get("InvestToken").str
  def getTelegramToken : String = get("TelegramToken").str
}
