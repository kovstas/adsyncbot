package dev.kovstas.adsyncbot

import canoe.api._
import cats.effect.{ExitCode, IO, IOApp, Resource}
import dev.kovstas.adsyncbot.config.AppConfig
import dev.kovstas.adsyncbot.resources._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      (_, appConfig) <- ConfigLoader.load[AppConfig]
      httpClient <- HttpClient.make[IO]
      _ <- DB.migrateDb(appConfig.db)
      transactor <- DB.transactor[IO](appConfig.db)
      repos = Repos(transactor)

      implicit0(logger: SelfAwareStructuredLogger[IO]) <- Resource.eval(
        Slf4jLogger.create[IO]
      )

      implicit0(telegramClient: TelegramClient[IO]) = TelegramClient
        .fromHttp4sClient[IO](
          appConfig.token
        )(httpClient)

      appServices = AppServices(repos, httpClient, appConfig.ms)
      _ <- Processes(appServices)

      _ <- HttpServer.make[IO](
        appConfig.port,
        appConfig.botUri,
        appServices.oAuthService
      )

    } yield ()).use(_ => IO.never).as(ExitCode.Success)
  }
}
