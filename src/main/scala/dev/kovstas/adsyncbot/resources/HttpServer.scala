package dev.kovstas.adsyncbot.resources

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.{Applicative, Monad}
import com.comcast.ip4s._
import dev.kovstas.adsyncbot.auth.{OAuthRoute, OAuthService}
import org.http4s.{HttpApp, HttpRoutes, Response, Uri}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.http4s.server.{Router, Server}

object HttpServer {

  def make[F[_]: Async](
      port: Port,
      botUri: Uri,
      oAuthService: OAuthService[F]
  ): Resource[F, Server] = {
    val app = makeHttpApp(botUri, oAuthService)

    EmberServerBuilder
      .default[F]
      .withoutHost
      .withPort(port)
      .withHttpApp(app)
      .build
  }

  private def makeHttpApp[F[_]: Async](
      botUri: Uri,
      oAuthService: OAuthService[F]
  ): HttpApp[F] = {
    Logger.httpApp(logHeaders = true, logBody = true)(
      Router(
        "/api/v1/" -> route,
        "/api/v1/auth/" -> OAuthRoute.make(botUri, oAuthService)
      ).orNotFound
    )
  }

  private def route[F[_]: Monad]: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @GET -> Root / "health" =>
      Applicative[F].pure(Response[F](Ok))
  }

}
