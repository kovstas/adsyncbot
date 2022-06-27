package dev.kovstas.adsyncbot.az.model

import cats.effect.kernel.Concurrent
import dev.kovstas.adsyncbot.organization.Organization.AdTenantId
import io.circe.generic.JsonCodec
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

@JsonCodec(decodeOnly = true)
final case class OrganizationResponse(
    id: AdTenantId,
    displayName: String
)

object OrganizationResponse {
  implicit def decoder[F[_]: Concurrent]
      : EntityDecoder[F, OrganizationResponse] =
    jsonOf[F, OrganizationResponse]
}
