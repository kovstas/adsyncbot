package dev.kovstas.burningbot.config

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
  val authoriseLink: Uri =
    (host / tenantId / "adminconsent").withQueryParams(
      Map(
        "client_id" -> clientId,
        "redirect_uri" -> redirectUri.renderString,
        "state" -> "12345"
      )
    )
}

object MsAuthConfig extends PureConfig {
  import pureconfig.module.http4s.uriReader
  implicit val reader: ConfigReader[MsAuthConfig] = deriveReader
}
