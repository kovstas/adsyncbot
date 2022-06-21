package dev.kovstas.adsyncbot

import canoe.api._
import cats.effect.{ExitCode, IO, IOApp, Resource}
import dev.kovstas.adsyncbot.auth.{DefaultMsAuthClient, DefaultMsAuthService}
import dev.kovstas.adsyncbot.config.AppConfig
import dev.kovstas.adsyncbot.resources.{
  ConfigLoader,
  DB,
  HttpClient,
  HttpServer
}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      (_, appConfig) <- ConfigLoader.load[AppConfig]
      httpClient <- HttpClient.make[IO]
      _ <- DB.migrateDb(appConfig.db)
      _ <- DB.transactor[IO](appConfig.db)

      implicit0(client: TelegramClient[IO]) = TelegramClient
        .fromHttp4sClient[IO](
          appConfig.token
        )(httpClient)

      appServices = AppServices()

      scenario = new MainScenario(
        appServices.userService,
        //appServices.companyService,
        appConfig.ms
      )

      _ <- HttpServer.make[IO](
        appConfig.port,
        HttpServer.makeHttpApp(
          appConfig.botUri,
          new DefaultMsAuthService(
            new DefaultMsAuthClient(httpClient, appConfig.ms),
            telegramClient = client,
            companyService = appServices.companyService,
            userService = appServices.userService
          )
        )
      )

      _ <- Resource.eval(
        Bot
          .polling[IO]
          .follow(scenario.start)
          .compile
          .drain
      )
    } yield ()).use(_ => IO.never).as(ExitCode.Success)
  }
}
