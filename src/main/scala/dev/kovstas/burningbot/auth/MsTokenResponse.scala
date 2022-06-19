package dev.kovstas.burningbot.auth

import cats.effect.IO
import io.circe.generic.JsonCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

@JsonCodec
final case class MsTokenResponse(
    accessToken: String
)

object MsTokenResponse {
  implicit lazy val ssoTokenDecoder: EntityDecoder[IO, MsTokenResponse] =
    jsonOf[IO, MsTokenResponse]
}
