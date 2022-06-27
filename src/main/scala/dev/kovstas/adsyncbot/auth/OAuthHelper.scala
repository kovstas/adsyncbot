package dev.kovstas.adsyncbot.auth

import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.user.User.UserId
import org.http4s.Uri

object OAuthHelper {

  def organizationAuthoriseLink(config: MsConfig, userId: UserId): Uri =
    (config.loginUri / "common" / "adminconsent").withQueryParams(
      Map(
        "client_id" -> config.clientId,
        "redirect_uri" -> config.organizationLoginRedirect.renderString,
        "state" -> userId.value.toString
      )
    )

  def organizationMemberAuthoriseLink(
      config: MsConfig,
      userId: UserId
  ): Uri =
    (config.loginUri / "organizations" / "oauth2" / "v2.0" / "authorize")
      .withQueryParams(
        Map(
          "client_id" -> config.clientId,
          "response_type" -> "token",
          "response_mode" -> "form_post",
          "redirect_uri" -> config.organizationMemberLoginRedirect.renderString,
          "scope" -> "user.read openid profile email",
          "state" -> userId.value.toString
        )
      )

}
