package dev.kovstas.burningbot.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader
import scala.concurrent.duration.FiniteDuration

final case class DbConfig(
    url: String,
    user: String,
    pass: String,
    connectionTimeout: FiniteDuration,
    minConnections: Int,
    maxConnections: Int
)

object DbConfig extends PureConfig {
  implicit val reader: ConfigReader[DbConfig] = deriveReader
}
