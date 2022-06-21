package dev.kovstas.adsyncbot.model

import dev.kovstas.adsyncbot.model.Company.{ADTenantId, CompanyId}
import dev.kovstas.adsyncbot.user.User.UserId
import io.estatico.newtype.macros.newtype
import java.time.Instant

final case class Company(
    id: CompanyId,
    adTenantId: ADTenantId,
    createdBy: UserId,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object Company {
  @newtype case class CompanyId(value: String)
  @newtype case class ADTenantId(value: String)
}
