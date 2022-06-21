package dev.kovstas.adsyncbot.config

import dev.kovstas.adsyncbot.user.User.UserId
import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class MsAuthConfig(
    host: Uri,
    tenantId: String,
    clientId: String,
    clientSecret: String,
    redirectUri: Uri
) {
  def authoriseLink(userId: UserId): Uri =
    (host / tenantId / "adminconsent").withQueryParams(
      Map(
        "client_id" -> clientId,
        "redirect_uri" -> redirectUri.renderString,
        "state" -> s"${userId.value}"
      )
    )
}

object MsAuthConfig extends PureConfig {
  import pureconfig.module.http4s.uriReader
  implicit val reader: ConfigReader[MsAuthConfig] = deriveReader
}
