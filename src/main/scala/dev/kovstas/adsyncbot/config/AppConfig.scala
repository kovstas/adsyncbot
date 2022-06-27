package dev.kovstas.adsyncbot.config

import com.comcast.ip4s.Port
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class AppConfig(
    port: Port,
    tg: TelegramConfig,
    db: DbConfig,
    ms: MsConfig
)

object AppConfig extends PureConfig {
  import pureconfig.module.ip4s.portReader
  implicit val reader: ConfigReader[AppConfig] = deriveReader
}
