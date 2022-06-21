package dev.kovstas.adsyncbot.auth

import cats.effect.kernel.Concurrent
import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

@ConfiguredJsonCodec
final case class MsErrorResponse(
    error: String,
    errorDescription: String
)

object MsErrorResponse {
  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, MsErrorResponse] =
    jsonOf[F, MsErrorResponse]
}
