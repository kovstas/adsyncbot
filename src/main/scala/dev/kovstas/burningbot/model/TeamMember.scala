package dev.kovstas.burningbot.model

import dev.kovstas.burningbot.model.Team.TeamId
import dev.kovstas.burningbot.model.TeamMember.{
  AdUserId,
  TeamMemberId,
  TgUserId
}
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class TeamMember(
    id: TeamMemberId,
    teamId: TeamId,
    name: String,
    email: String,
    phoneNumber: Option[String],
    isActive: Boolean,
    role: TeamMemberRole,
    tgUserId: TgUserId,
    tgChatId: TgChatId,
    adUserId: AdUserId,
    createdAt: Instant,
    updatedAt: Instant
)

object TeamMember {
  @newtype case class TeamMemberId(value: UUID)
  @newtype case class TgUserId(value: Long)
  @newtype case class AdUserId(value: String)

}
