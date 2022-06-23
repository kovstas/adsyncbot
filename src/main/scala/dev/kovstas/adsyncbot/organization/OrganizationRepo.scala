package dev.kovstas.adsyncbot.organization

import dev.kovstas.adsyncbot.organization.Organization.{
  ADTenantId,
  OrganizationId
}
import dev.kovstas.adsyncbot.organization.OrganizationMember.AdUserId
import dev.kovstas.adsyncbot.user.User.UserId

trait OrganizationRepo[F[_]] {

  def insert(
      tenantId: ADTenantId,
      name: String,
      createdBy: UserId
  ): F[OrganizationId]

  def getOrganizationByUserId(userId: UserId): F[Option[Organization]] // TODO work with many orgs

  def getOrganization(tenantId: ADTenantId): F[Option[Organization]]

  def insertMember(
      organizationId: OrganizationId,
      userId: UserId,
      adUserId: AdUserId,
      email: String,
      name: String,
      phoneNumber: Option[String]
  ): F[Unit]
}
