package dev.kovstas.adsyncbot.config

import com.comcast.ip4s.Port
import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class AppConfig(
    port: Port,
    botUri: Uri,
    token: String,
    db: DbConfig,
    ms: MsAuthConfig
)

object AppConfig extends PureConfig {
  import pureconfig.module.ip4s.portReader
  import pureconfig.module.http4s.uriReader
  implicit val reader: ConfigReader[AppConfig] = deriveReader
}
