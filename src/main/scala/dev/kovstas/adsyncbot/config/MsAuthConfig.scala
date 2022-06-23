package dev.kovstas.adsyncbot.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class MsAuthConfig(
    host: Uri,
    clientId: String,
    clientSecret: String,
    organizationLoginRedirect: Uri,
    organizationMemberLoginRedirect: Uri
)

object MsAuthConfig extends PureConfig {
  implicit val reader: ConfigReader[MsAuthConfig] = deriveReader
}
