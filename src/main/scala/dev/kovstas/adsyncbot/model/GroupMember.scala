package dev.kovstas.adsyncbot.model

import dev.kovstas.adsyncbot.model.CompanyUser.CompanyUserId
import dev.kovstas.adsyncbot.model.Group.GroupId
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class GroupMember(
    groupId: GroupId,
    companyUserId: CompanyUserId,
    createdAt: Instant
)

object GroupMember {
  @newtype case class GroupMemberId(value: UUID)

}
