package dev.kovstas.burningbot.auth

import cats.Monad
import dev.kovstas.burningbot.model.Team.ADTenantId
import dev.kovstas.burningbot.model.TgChatId
import org.http4s.Method.GET
import org.http4s.dsl.io.{QueryParamDecoderMatcher, _}
import org.http4s.{HttpRoutes, QueryParamDecoder, Response}
import cats.syntax.all._
import org.http4s.headers.Location
import org.http4s.implicits.http4sLiteralsSyntax

object MsAuthRoute {

  object TenantIdParam
      extends QueryParamDecoderMatcher[ADTenantId]("tenant")(
        QueryParamDecoder.stringQueryParamDecoder.map(ADTenantId.apply)
      )
  object AdminConsentParam
      extends QueryParamDecoderMatcher[Boolean]("admin_consent")
  object TgChatIdParam
      extends QueryParamDecoderMatcher[TgChatId]("state")(
        QueryParamDecoder.longQueryParamDecoder.map(TgChatId.apply)
      )

  //admin_consent=True&tenant=865fdd56-6bb6-4a1a-9829-88c29f943357&state=12345#

  def make[F[_]: Monad](msAuthService: MsAuthService[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case _ @GET -> Root / "ms-tenant-login" :?
          AdminConsentParam(adminConsent) +&
          TenantIdParam(tenantId) +&
          TgChatIdParam(chatId) =>
        msAuthService
          .tenantLogin(adminConsent, tenantId, chatId)
          .as(
            Response[F]()
              .withStatus(MovedPermanently)
              .withHeaders(Location(uri"https://t.me/sre_burning_bot"))
          )

    }

}
