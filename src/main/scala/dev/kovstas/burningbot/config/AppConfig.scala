package dev.kovstas.burningbot.config

import com.comcast.ip4s.Port
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class AppConfig(
    port: Port,
    token: String,
    db: DbConfig,
    ms: MsAuthConfig
)

object AppConfig extends PureConfig {
  import pureconfig.module.ip4s.portReader
  implicit val reader: ConfigReader[AppConfig] = deriveReader
}
