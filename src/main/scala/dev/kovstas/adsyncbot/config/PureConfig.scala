package dev.kovstas.adsyncbot.config

import pureconfig.{CamelCase, ConfigFieldMapping}
import pureconfig.generic.ProductHint

trait PureConfig {
  implicit def camelCaseHint[T]: ProductHint[T] =
    ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
}
