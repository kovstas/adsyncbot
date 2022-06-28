package dev.kovstas.adsyncbot.organization

import dev.kovstas.adsyncbot.organization.Organization.{
  AdTenantId,
  OrganizationId
}
import dev.kovstas.adsyncbot.user.User.UserId
import doobie.postgres.implicits._
import doobie.Meta
import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class Organization(
    id: OrganizationId,
    adTenantId: AdTenantId,
    name: String,
    createdBy: UserId,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object Organization {

  @newtype case class OrganizationId(value: UUID)
  object OrganizationId {
    implicit val meta: Meta[OrganizationId] = deriving[Meta]
  }

  @newtype case class AdTenantId(value: String)
  object AdTenantId {
    implicit val decoder: Decoder[AdTenantId] = deriving[Decoder]
    implicit val meta: Meta[AdTenantId] = deriving[Meta]
  }

}
