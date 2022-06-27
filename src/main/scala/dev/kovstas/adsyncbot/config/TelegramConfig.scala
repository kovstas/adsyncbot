package dev.kovstas.adsyncbot.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class TelegramConfig(
    botUri: Uri,
    botId: Long,
    token: String
)

object TelegramConfig extends PureConfig {
  import pureconfig.module.http4s.uriReader
  implicit val reader: ConfigReader[TelegramConfig] = deriveReader
}
