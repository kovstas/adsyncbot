package dev.kovstas.adsyncbot.auth

import cats.effect.kernel.Concurrent
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.model.{MsErrorResponse, MsTokenResponse}
import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.organization.Organization.AdTenantId
import org.http4s.Method.POST
import org.http4s._
import org.http4s.client.Client

trait OAuthClient[F[_]] {
  def loginOrganization(
      tenantId: AdTenantId
  ): F[MsTokenResponse]
}

final class DefaultOAuthClient[F[_]: Concurrent](
    httpClient: Client[F],
    config: MsConfig
) extends OAuthClient[F] {

  override def loginOrganization(tenantId: AdTenantId): F[MsTokenResponse] = {
    val request = Request[F](
      POST,
      config.loginUri / tenantId.value / "oauth2" / "v2.0" / "token"
    ).withEntity(
      UrlForm(
        "client_id" -> config.clientId,
        "client_secret" -> config.clientSecret,
        "scope" -> "https://graph.microsoft.com/.default",
        "grant_type" -> "client_credentials"
      )
    )
    httpClient.expectOr[MsTokenResponse](request)(defaultErrorHandler)
  }

  private val defaultErrorHandler: Response[F] => F[Throwable] = res => {
    EntityDecoder[F, MsErrorResponse]
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
