package dev.kovstas.adsyncbot.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class MsConfig(
    clientId: String,
    clientSecret: String,
    loginUri: Uri,
    graphUri: Uri,
    organizationLoginRedirect: Uri,
    organizationMemberLoginRedirect: Uri
)

object MsConfig extends PureConfig {
  import pureconfig.module.http4s.uriReader
  implicit val reader: ConfigReader[MsConfig] = deriveReader
}
