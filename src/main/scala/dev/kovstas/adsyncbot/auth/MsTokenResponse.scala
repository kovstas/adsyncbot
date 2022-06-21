package dev.kovstas.adsyncbot.auth

import cats.effect.kernel.Concurrent
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

@ConfiguredJsonCodec
final case class MsTokenResponse(
    accessToken: String
)

object MsTokenResponse {
  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, MsTokenResponse] =
    jsonOf[F, MsTokenResponse]
}
