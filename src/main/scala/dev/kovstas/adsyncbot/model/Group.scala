package dev.kovstas.adsyncbot.model

import dev.kovstas.adsyncbot.model.Company.CompanyId
import dev.kovstas.adsyncbot.model.Group._
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class Group(
    id: GroupId,
    name: GroupName,
    companyId: CompanyId,
    tgChatId: TgChatId,
    adGroupId: AdGroupId,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object Group {
  @newtype case class GroupId(value: UUID)
  @newtype case class AdGroupId(value: String)
  @newtype case class GroupName(value: String)
}
