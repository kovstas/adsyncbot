package dev.kovstas.burningbot

import canoe.api._
import cats.effect.{ExitCode, IO, IOApp, Resource}
import dev.kovstas.burningbot.auth.{DefaultMsAuthClient, DefaultMsAuthService}
import dev.kovstas.burningbot.config.AppConfig
import dev.kovstas.burningbot.resources.{
  ConfigLoader,
  DB,
  HttpClient,
  HttpServer
}
import dev.kovstas.burningbot.teamMember.PostgresTeamMemberRepo

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      (_, appConfig) <- ConfigLoader.load[AppConfig]
      _ <- DB.migrateDb(appConfig.db)
      _ <- DB.transactor[IO](appConfig.db)
      httpClient <- HttpClient.make[IO]
      _ <- HttpServer.make[IO](
        appConfig.port,
        HttpServer.makeHttpApp(
          new DefaultMsAuthService(
            new DefaultMsAuthClient(httpClient, appConfig.ms)
          )
        )
      )
      implicit0(client: TelegramClient[IO]) = TelegramClient
        .fromHttp4sClient[IO](
          appConfig.token
        )(httpClient)
      scenario = new MainScenario(
        new PostgresTeamMemberRepo[IO](),
        appConfig.ms
      )
      _ <- Resource.eval(
        Bot
          .polling[IO]
          .follow(scenario.start)
          .through(scenario.answerCallbacks)
          .compile
          .drain
      )
    } yield ()).use(_ => IO.never).as(ExitCode.Success)
  }
}
