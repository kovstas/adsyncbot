package dev.kovstas.adsyncbot.auth

import dev.kovstas.adsyncbot.config.MsAuthConfig
import dev.kovstas.adsyncbot.user.User.UserId
import org.http4s.Uri

object OAuthHelper {

  def companyAuthoriseLink(config: MsAuthConfig, userId: UserId): Uri =
    (config.host / "organizations" / "adminconsent").withQueryParams(
      Map(
        "client_id" -> config.clientId,
        "redirect_uri" -> config.organizationLoginRedirect.renderString,
        "state" -> userId.value.toString
      )
    )

  def companyMemberAuthoriseLink(
      config: MsAuthConfig,
      userId: UserId
  ): Uri =
    (config.host / "organizations" / "oauth2" / "v2.0" / "authorize")
      .withQueryParams(
        Map(
          "client_id" -> config.clientId,
          "response_type" -> "token",
          "redirect_uri" -> config.organizationMemberLoginRedirect.renderString,
          "scope" -> "user.read openid profile email",
          "state" -> userId.value.toString
        )
      )

}
