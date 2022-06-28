package dev.kovstas.adsyncbot.az.model

import cats.effect.kernel.Concurrent
import dev.kovstas.adsyncbot.organization.OrganizationMember.AdUserId
import io.circe.generic.JsonCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

@JsonCodec(decodeOnly = true)
final case class UserResponse(
    id: AdUserId,
    businessPhones: List[String],
    displayName: String,
    mail: String,
    accountEnabled: Boolean
)

object UserResponse {
  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, UserResponse] =
    jsonOf[F, UserResponse]
}
