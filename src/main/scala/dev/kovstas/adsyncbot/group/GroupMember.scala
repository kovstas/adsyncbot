package dev.kovstas.adsyncbot.group

import dev.kovstas.adsyncbot.group.Group.GroupId
import dev.kovstas.adsyncbot.organization.OrganizationMember.OrganizationMemberId
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class GroupMember(
    groupId: GroupId,
    organizationMemberId: OrganizationMemberId,
    createdAt: Instant
)

object GroupMember {
  @newtype case class GroupMemberId(value: UUID)

}
