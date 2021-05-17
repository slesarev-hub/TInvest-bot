import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  val bot = new Bot
  val eol = bot.run()
  println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
  scala.io.StdIn.readLine()
  bot.shutdown()
  Await.result(eol, Duration.Inf)
}
