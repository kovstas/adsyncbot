package dev.kovstas.adsyncbot.organization

import dev.kovstas.adsyncbot.organization.Organization.{
  ADTenantId,
  OrganizationId
}
import dev.kovstas.adsyncbot.user.User.UserId
import doobie.Meta
import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import java.time.Instant

final case class Organization(
    id: OrganizationId,
    adTenantId: ADTenantId,
    name: String,
    createdBy: UserId,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object Organization {
  @newtype case class OrganizationId(value: String)
  @newtype case class ADTenantId(value: String)

  object ADTenantId {
    implicit val decoder: Decoder[ADTenantId] = deriving[Decoder]
    implicit val meta: Meta[ADTenantId] = deriving[Meta]
  }

}
