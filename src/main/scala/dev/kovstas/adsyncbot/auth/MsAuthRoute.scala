package dev.kovstas.adsyncbot.auth

import cats.Monad
import cats.syntax.all._
import dev.kovstas.adsyncbot.model.Company.ADTenantId
import dev.kovstas.adsyncbot.user.User.UserId
import java.util.UUID
import org.http4s.Method.GET
import org.http4s.dsl.io.{QueryParamDecoderMatcher, _}
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, ParseFailure, QueryParamDecoder, Response, Uri}
import scala.util.Try

object MsAuthRoute {

  object TenantIdParam
      extends QueryParamDecoderMatcher[ADTenantId]("tenant")(
        QueryParamDecoder.stringQueryParamDecoder.map(ADTenantId.apply)
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

  def make[F[_]: Monad](
      botUri: Uri,
      msAuthService: MsAuthService[F]
  ): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case _ @GET -> Root / "company-login" :?
          AdminConsentParam(adminConsent) +&
          TenantIdParam(tenantId) +&
          UserIdParam(userId) =>
        msAuthService
          .companyLogin(adminConsent, tenantId, userId)
          .as(
            Response[F]()
              .withStatus(MovedPermanently)
              .withHeaders(Location(botUri))
          )

    }

}
