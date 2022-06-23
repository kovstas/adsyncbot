package dev.kovstas.adsyncbot.az

import dev.kovstas.adsyncbot.az.model.{
  GroupsResponse,
  OrganizationResponse,
  UserResponse
}
import dev.kovstas.adsyncbot.organization.Organization.ADTenantId
import dev.kovstas.adsyncbot.organization.OrganizationMember.AdUserId

trait ApplicationGraphApi[F[_]] {
  def groups(accessToken: String): F[GroupsResponse]
  def user(accessToken: String, adUserId: AdUserId): F[UserResponse]
  def organization(
      accessToken: String,
      tenantId: ADTenantId
  ): F[OrganizationResponse]
}
