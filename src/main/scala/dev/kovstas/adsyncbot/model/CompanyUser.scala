package dev.kovstas.adsyncbot.model

import dev.kovstas.adsyncbot.model.Company.CompanyId
import dev.kovstas.adsyncbot.model.CompanyUser.{AdUserId, CompanyUserId}
import dev.kovstas.adsyncbot.user.User.UserId
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class CompanyUser(
    id: CompanyUserId,
    userId: UserId,
    companyId: CompanyId,
    adUserId: AdUserId,
    email: Option[String],
    phoneNumber: Option[String],
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object CompanyUser {
  @newtype case class CompanyUserId(value: UUID)
  @newtype case class AdUserId(value: String)
}
