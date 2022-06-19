package dev.kovstas.burningbot.resources

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.{Applicative, Monad}
import com.comcast.ip4s._
import dev.kovstas.burningbot.auth.{MsAuthRoute, MsAuthService}
import org.http4s.{HttpApp, HttpRoutes, Response}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.{Router, Server}

object HttpServer {

  def makeHttpApp[F[_]: Async](msAuthService: MsAuthService[F]): HttpApp[F] = {
    Logger.httpApp(logHeaders = true, logBody = true)(
      Router(
        "/api/v1/" -> route,
        "/api/v1/auth/" -> MsAuthRoute.make(msAuthService)
      ).orNotFound
    )
  }

  def make[F[_]: Async](
      port: Port,
      app: HttpApp[F]
  ): Resource[F, Server] = {
    EmberServerBuilder
      .default[F]
      .withoutHost
      .withPort(port)
      .withHttpApp(app)
      .build
  }

  private def route[F[_]: Monad]: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @GET -> Root / "health" =>
      Applicative[F].pure(Response[F](Ok))
  }

}
