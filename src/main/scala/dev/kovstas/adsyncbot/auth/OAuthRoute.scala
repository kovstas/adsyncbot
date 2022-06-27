package dev.kovstas.adsyncbot.auth

import cats.effect.kernel.Concurrent
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.model.OrganizationMemberLoginRequest
import dev.kovstas.adsyncbot.organization.Organization.AdTenantId
import dev.kovstas.adsyncbot.user.User.UserId
import java.util.UUID
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.dsl.io.{QueryParamDecoderMatcher, _}
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder, Response, Uri}
import scala.util.Try

object OAuthRoute {

  object TenantIdParam
      extends QueryParamDecoderMatcher[AdTenantId]("tenant")(
        QueryParamDecoder.stringQueryParamDecoder.map(AdTenantId.apply)
      )
  object AdminConsentParam
      extends QueryParamDecoderMatcher[Boolean]("admin_consent")

  object UserIdParam
      extends QueryParamDecoderMatcher[UserId]("state")(
        QueryParamDecoder.stringQueryParamDecoder.emap(s =>
          Try(UUID.fromString(s))
            .map(UserId.apply)
            .toEither
            .leftMap(e =>
              ParseFailure("State doesn't have a correct userId", e.getMessage)
            )
        )
      )

  def make[F[_]: Concurrent](
      botUri: Uri,
      oAuthService: OAuthService[F]
  ): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case _ @GET -> Root / "organization-login" :?
          AdminConsentParam(adminConsent) +&
          TenantIdParam(tenantId) +&
          UserIdParam(userId) =>
        oAuthService
          .organizationLogin(adminConsent, tenantId, userId)
          .as(
            Response[F]()
              .withStatus(MovedPermanently)
              .withHeaders(Location(botUri))
          )
      case req @ POST -> Root / "organization-member-login" =>
        for {
          entity <- req.as[OrganizationMemberLoginRequest]
          _ <- oAuthService
            .organizationMemberLogin(entity.accessToken, entity.userId)
        } yield Response[F]()
          .withStatus(MovedPermanently)
          .withHeaders(Location(botUri))

    }

}
