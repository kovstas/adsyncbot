package dev.kovstas.burningbot.model

import dev.kovstas.burningbot.model.Team._
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class Team(
    id: TeamId,
    name: TeamName,
    tgChatId: TgChatId,
    isActive: Boolean,
    adTenantId: ADTenantId,
    adGroupId: ADGroupId,
    createdAt: Instant,
    updatedAt: Instant
)

object Team {
  @newtype case class TeamId(value: UUID)
  @newtype case class ADTenantId(value: String)
  @newtype case class ADGroupId(value: String)
  @newtype case class TeamName(value: String)
}
