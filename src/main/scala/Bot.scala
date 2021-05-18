import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.Message
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import exceptions.InvalidMessage
import openApi.SandboxMoneyAmount
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Bot extends TelegramBot with Polling with Commands[Future] {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  implicit val backend = OkHttpFutureBackend()
  override val client: RequestHandler[Future] = new FutureSttpClient(Env.getTelegramToken)
  private val investApi = new InvestApi()

  private def moneyOperation(f: SandboxMoneyAmount => Future[Void])(implicit msg: Message): Future[Unit] =
    withArgs {
      case Seq(moneyString) => SandboxMoneyAmount.fromString(moneyString)
        .flatMap {
          case Success(moneyAmount) => f(moneyAmount)
          case Failure(e) => Future.failed(InvalidMessage(e.getMessage))
        }
        .flatMap(_ => investApi.getBalance)
        .transform {
          case Success(balance) => Try(reply(ReplyDataHandler.balance(balance)))
          case Failure(exception) => Try(reply(exception.getMessage))
        }
      case _ => Future(reply("Invalid Message"))
    }

  private def stockOperation(op: String)(implicit msg: Message) = {
    withArgs {
      case Seq(ticker, lots) =>
        investApi.placeMarketOrder(op, ticker, lots.toInt)
          .transform {
            case Success(order) => Try(reply(ReplyDataHandler.stockOperation(ticker, order)))
            case Failure(exception) => Try(reply(exception.getMessage.toLowerCase))
          }
      case _ => Future(reply("Invalid Message"))
    }
  }
  onCommand(stringToCommandFilter("balance")) { implicit msg =>
    investApi.getBalance
      .map(balance => reply(ReplyDataHandler.balance(balance)))
  }

  onCommand(stringToCommandFilter("insert")) { implicit msg => moneyOperation(investApi.insertMoney)}

  onCommand(stringToCommandFilter("withdraw")) { implicit msg => moneyOperation(investApi.withdrawMoney)}

  onCommand(stringToCommandFilter("price")) { implicit msg =>
    withArgs{
      case Seq(ticker) => investApi.getPrice(ticker)
        .map(money => reply(ReplyDataHandler.tickerPrice(ticker.toUpperCase, money)))
        .transform{
          case Failure(exception) => Try(reply(exception.getMessage.toLowerCase))
        }
      case _ => Future(reply("Invalid Message"))
    }
  }

  onCommand(stringToCommandFilter("list")) { implicit msg =>
    investApi.getPortfolio
      .map(portfolio => reply(ReplyDataHandler.portfolio(portfolio)))
  }

  onCommand(stringToCommandFilter("sell")) { implicit msg => stockOperation("Sell")}

  onCommand(stringToCommandFilter("buy")) { implicit msg => stockOperation("Buy")}
}
