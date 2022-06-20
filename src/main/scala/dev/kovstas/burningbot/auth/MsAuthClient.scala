package dev.kovstas.burningbot.auth

import cats.effect.IO
import dev.kovstas.burningbot.config.MsAuthConfig
import dev.kovstas.burningbot.model.Team.ADTenantId
import org.http4s.Method.POST
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.dsl.io._

trait MsAuthClient[F[_]] {
  def tenantServiceToken(
      tenantId: ADTenantId
  ): F[MsTokenResponse]
}

final class DefaultMsAuthClient(httpClient: Client[IO], config: MsAuthConfig)
    extends MsAuthClient[IO] {
  override def tenantServiceToken(tenantId: ADTenantId): IO[MsTokenResponse] = {
    val request = POST(
      UrlForm(
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret,
        "scope" -> "https://graph.microsoft.com/.default",
        "grant_type" -> "client_credentials"
      ),
      config.host / tenantId.value / "oauth2" / "v2.0" / "token"
    )
    httpClient.expectOr[MsTokenResponse](request)(defaultErrorHandler)
  }

  private val defaultErrorHandler: Response[IO] => IO[Throwable] = res => {
    EntityDecoder[IO, MsErrorResponse]
      .decode(res, strict = true)
      .value
      .map {
        case Left(d) =>
          new IllegalStateException(d.getMessage())
        case Right(error) =>
          AuthenticationError(error.errorDescription)
      }
  }

}
