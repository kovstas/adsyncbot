package dev.kovstas.adsyncbot.resources

import cats.effect.kernel.Async
import cats.effect.{IO, Resource, Sync}
import cats.implicits.catsSyntaxOptionId
import dev.kovstas.adsyncbot.config.DbConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import fly4s.core.Fly4s
import fly4s.core.data.{Fly4sConfig, Location, MigrateResult}

object DB {

  def migrateDb(config: DbConfig): Resource[IO, MigrateResult] = Fly4s
    .make[IO](
      url = config.url,
      user = config.user.some,
      password = config.pass.toCharArray.some,
      config = Fly4sConfig(
        table = "flyway",
        locations = Location.of("db"),
        baselineOnMigrate = true
      )
    )
    .evalMap(_.migrate)

  def transactor[F[_]: Async](
      config: DbConfig
  ): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = "org.postgresql.Driver",
        config.url,
        config.user,
        config.pass,
        ce
      )
      _ <- Resource.eval[F, Unit](configure(xa)(config))
    } yield xa

  private def configure[F[_]: Sync](
      xa: HikariTransactor[F]
  )(config: DbConfig): F[Unit] =
    xa.configure { c =>
      Sync[F].delay {
        c.setAutoCommit(false)
        c.setPoolName("default-pool")
        c.setMaxLifetime(600000)
        c.setIdleTimeout(30000)
        c.setMaximumPoolSize(config.maxConnections)
        c.setMinimumIdle(config.minConnections)
      }
    }
}
