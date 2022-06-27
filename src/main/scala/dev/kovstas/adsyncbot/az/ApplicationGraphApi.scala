package dev.kovstas.adsyncbot.az

import cats.effect.kernel.Concurrent
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.OAuthClient
import dev.kovstas.adsyncbot.az.model.{OrganizationResponse, UserResponse}
import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.organization.Organization.AdTenantId
import dev.kovstas.adsyncbot.organization.OrganizationMember.AdUserId
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, Credentials, Request}

trait ApplicationGraphApi[F[_]] {
  def user(tenantId: AdTenantId, adUserId: AdUserId): F[Option[UserResponse]]
  def organization(
      tenantId: AdTenantId
  ): F[OrganizationResponse]
}

final class DefaultApplicationGraphApi[F[_]: Concurrent](
    auth: OAuthClient[F],
    httpClient: Client[F],
    config: MsConfig
) extends ApplicationGraphApi[F] {

  override def user(
      tenantId: AdTenantId,
      adUserId: AdUserId
  ): F[Option[UserResponse]] = {
    withAuth(tenantId) { auth =>
      val req = Request[F](
        GET,
        config.graphUri / "users" / adUserId.value
      ).withHeaders(auth)
      httpClient.expectOption(req)
    }
  }

  override def organization(tenantId: AdTenantId): F[OrganizationResponse] =
    withAuth(tenantId) { auth =>
      val req = Request[F](
        GET,
        config.graphUri / "organization" / tenantId.value
      ).withHeaders(auth)

      httpClient.expect(req)
    }

  private def withAuth[T](
      tenantId: AdTenantId
  )(f: Authorization => F[T]): F[T] = {
    auth
      .loginOrganization(tenantId)
      .flatMap(r =>
        f(Authorization(Credentials.Token(AuthScheme.Bearer, r.accessToken)))
      )
  }

}
