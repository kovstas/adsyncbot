package dev.kovstas.adsyncbot.az.model

import dev.kovstas.adsyncbot.organization.Organization.ADTenantId
import io.circe.generic.JsonCodec

@JsonCodec(decodeOnly = true)
final case class OrganizationResponse(
    id: ADTenantId,
    displayName: String
)
