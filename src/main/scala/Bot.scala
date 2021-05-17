import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import exceptions.InvalidMessage
import sandbox.SandboxMoneyAmount
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Bot extends TelegramBot with Polling with Commands[Future] {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  implicit val backend = OkHttpFutureBackend()
  override val client: RequestHandler[Future] = new FutureSttpClient(Env.getTelegramToken)
  val investApi = new InvestApi()

  onCommand(stringToCommandFilter("balance")) { implicit msg =>
    investApi.getBalance
      .map(balance => reply(ReplyDataHandler.balance(balance)))
  }

  onCommand(stringToCommandFilter("insert")) { implicit msg =>
    withArgs{
      case Seq(moneyString) =>
        SandboxMoneyAmount.fromString(moneyString)
          .flatMap{
            case Success(moneyAmount) => investApi.insertMoney(moneyAmount)
            case Failure(e) => Future.failed(InvalidMessage(e.getMessage))
          }
          .flatMap(_ => investApi.getBalance)
          .transform{
            case Success(balance) => Try(reply(ReplyDataHandler.balance(balance)))
            case Failure(exception) => Try(reply(exception.getMessage))
          }
    }
  }

}
