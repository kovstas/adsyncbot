package dev.kovstas.adsyncbot.resources

import cats.data.EitherT
import cats.effect.{IO, Resource}
import com.typesafe.config.Config
import pureconfig.backend.ConfigWrapper.SafeConfig
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.ConfigReaderException
import scala.reflect.ClassTag

object ConfigLoader {

  def load[C: ConfigReader: ClassTag]: Resource[IO, (Config, C)] =
    Resource.eval(
      EitherT(IO.blocking(ConfigSource.default.config()))
        .subflatMap(c =>
          c.resolveSafe()
            .map(_.root())
            .flatMap(ConfigReader[C].from)
            .map(a => (c, a))
        )
        .leftMap(ConfigReaderException[C])
        .rethrowT
    )

}
