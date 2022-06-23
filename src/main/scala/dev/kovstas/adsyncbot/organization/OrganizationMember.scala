package dev.kovstas.adsyncbot.organization

import dev.kovstas.adsyncbot.organization.Organization.OrganizationId
import dev.kovstas.adsyncbot.organization.OrganizationMember.{
  AdUserId,
  OrganizationMemberId
}
import dev.kovstas.adsyncbot.user.User.UserId
import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class OrganizationMember(
    id: OrganizationMemberId,
    userId: UserId,
    organizationId: OrganizationId,
    adUserId: AdUserId,
    name: String,
    email: String,
    phoneNumber: Option[String],
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object OrganizationMember {
  @newtype case class OrganizationMemberId(value: UUID)
  @newtype case class AdUserId(value: String)
  object AdUserId {
    implicit val decoder: Decoder[AdUserId] = deriving[Decoder]
  }
}
