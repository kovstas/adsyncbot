package dev.kovstas.adsyncbot.az

import cats.syntax.all._
import cats.effect.kernel.Concurrent
import dev.kovstas.adsyncbot.az.model.{
  OrganizationResponse,
  UserResponse,
  ValueResponse
}
import dev.kovstas.adsyncbot.config.MsConfig
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Request}

trait UserGraphApi[F[_]] {
  def organization(accessToken: String): F[OrganizationResponse]
  def me(accessToken: String): F[UserResponse]
}

final class DefaultUserGraphApi[F[_]: Concurrent](
    httpClient: Client[F],
    config: MsConfig
) extends UserGraphApi[F] {

  override def organization(
      accessToken: String
  ): F[OrganizationResponse] = {
    val req = Request[F](
      GET,
      config.graphUri / "organization"
    ).withHeaders(
      Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
    )

    httpClient.expect[ValueResponse[OrganizationResponse]](req).map(_.value)
  }

  override def me(accessToken: String): F[UserResponse] = {
    val req = Request[F](
      GET,
      config.graphUri / "me"
    ).withHeaders(
      Authorization(Credentials.Token(AuthScheme.Bearer, accessToken))
    )

    httpClient.expect(req)
  }
}
