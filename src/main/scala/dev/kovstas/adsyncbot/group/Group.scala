package dev.kovstas.adsyncbot.group

import dev.kovstas.adsyncbot.group.Group._
import dev.kovstas.adsyncbot.organization.Organization.OrganizationId
import dev.kovstas.adsyncbot.telegram.TgChatId
import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class Group(
    id: GroupId,
    name: GroupName,
    organizationId: OrganizationId,
    tgChatId: TgChatId,
    adGroupId: AdGroupId,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object Group {
  @newtype case class GroupId(value: UUID)
  @newtype case class AdGroupId(value: String)
  object AdGroupId {
    implicit val decoder: Decoder[AdGroupId] = deriving[Decoder]
  }
  @newtype case class GroupName(value: String)
}
