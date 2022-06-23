package dev.kovstas.adsyncbot.az.model

import dev.kovstas.adsyncbot.organization.OrganizationMember.AdUserId
import io.circe.generic.JsonCodec

@JsonCodec(decodeOnly = true)
final case class UserResponse(
    id: AdUserId,
    businessPhones: List[String],
    displayName: String,
    mail: String
)
