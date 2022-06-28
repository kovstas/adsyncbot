package dev.kovstas.adsyncbot.organization

import cats.effect.MonadCancelThrow
import cats.implicits.toFunctorOps
import dev.kovstas.adsyncbot.organization.Organization.{
  AdTenantId,
  OrganizationId
}
import dev.kovstas.adsyncbot.organization.OrganizationMember.{
  AdUserId,
  OrganizationMemberId
}
import doobie.postgres.implicits._
import doobie.implicits._
import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId
import java.time.Clock
import doobie.Transactor
import java.util.UUID

trait OrganizationRepo[F[_]] {

  def insert(
      tenantId: AdTenantId,
      name: String,
      createdBy: UserId
  ): F[OrganizationId]

  def getOrganizationByUserId(
      userId: UserId
  ): F[Option[Organization]]

  def getOrganizationMemberByTgUserId(
      tgUserId: TgChatId
  ): F[Option[OrganizationMember]]

  def getOrganizationMemberByUserId(
      userId: UserId,
      organizationId: OrganizationId
  ): F[Option[OrganizationMember]]

  def getOrganization(tenantId: AdTenantId): F[Option[Organization]]

  def insertMember(
      organizationId: OrganizationId,
      userId: UserId,
      adUserId: AdUserId,
      email: String,
      name: String,
      phoneNumber: Option[String]
  ): F[OrganizationMemberId]

  def getActiveOrganizations: F[List[Organization]]
  def getActiveOrganizationMembers(
      id: OrganizationId
  ): F[List[OrganizationMember]]
  def deactivateMember(id: OrganizationMemberId): F[Unit]
  def getTgMemberId(id: OrganizationMemberId): F[Option[TgChatId]]
}

final class PostgresOrganizationRepo[F[_]: MonadCancelThrow](
    clock: Clock,
    transactor: Transactor[F]
) extends OrganizationRepo[F] {

  override def insert(
      tenantId: AdTenantId,
      name: String,
      createdBy: UserId
  ): F[OrganizationId] = {
    val now = clock.instant()
    val id = OrganizationId(UUID.randomUUID())

    sql"""
       INSERT INTO organization (id, ad_tenant_id, name, created_by, is_active, created_at, updated_at)
       VALUES ($id, $tenantId, $name, $createdBy, true, $now, $now)
    """.update.run.transact(transactor).as(id)
  }

  override def getOrganizationByUserId(
      userId: UserId
  ): F[Option[Organization]] =
    sql"SELECT id, ad_tenant_id, name, created_by, is_active, created_at, updated_at FROM organization WHERE created_by = $userId"
      .query[Organization]
      .option
      .transact(transactor)

  override def getOrganizationMemberByTgUserId(
      tgUserId: TgChatId
  ): F[Option[OrganizationMember]] =
    sql"""
         SELECT om.id, om.user_id, om.organization_id, om.ad_tenant_id, om.name, om.email, om.phone_number, om.is_active, om.created_at, om.updated_at
         FROM organization_member om
         INNER JOIN "user" u ON u.id = om.user_id
         WHERE u.tg_chat_id = $tgUserId
       """.query[OrganizationMember].option.transact(transactor)

  override def getOrganization(tenantId: AdTenantId): F[Option[Organization]] =
    sql"SELECT id, ad_tenant_id, name, created_by, is_active, created_at, updated_at FROM organization WHERE ad_tenant_id = $tenantId"
      .query[Organization]
      .option
      .transact(transactor)

  override def insertMember(
      organizationId: OrganizationId,
      userId: UserId,
      adUserId: AdUserId,
      email: String,
      name: String,
      phoneNumber: Option[String]
  ): F[OrganizationMemberId] = {
    val now = clock.instant()
    val id = OrganizationMemberId(UUID.randomUUID())

    sql"""
        INSERT INTO organization_member (id, user_id, organization_id, ad_tenant_id, name, email, phone_number, is_active, created_at, updated_at)
        VALUES ($id, $userId, $organizationId, $adUserId, $name, $email, $phoneNumber, true, $now, $now)
    """.update.run.transact(transactor).as(id)
  }

  override def getActiveOrganizations: F[List[Organization]] =
    sql"SELECT id, ad_tenant_id, name, created_by, is_active, created_at, updated_at FROM organization WHERE is_active IS true"
      .query[Organization]
      .to[List]
      .transact(transactor)

  override def getActiveOrganizationMembers(
      id: OrganizationId
  ): F[List[OrganizationMember]] =
    sql"""
         SELECT id, user_id, organization_id, ad_tenant_id, name, email, phone_number, is_active, created_at, updated_at
         FROM organization_member
         WHERE is_active IS true
     """.query[OrganizationMember].to[List].transact(transactor)

  override def deactivateMember(id: OrganizationMemberId): F[Unit] = {
    val now = clock.instant()
    sql"""
        UPDATE organization_member
        SET is_active = false, updated_at = $now
        WHERE id = $id
    """.update.run.transact(transactor).void
  }

  override def getTgMemberId(id: OrganizationMemberId): F[Option[TgChatId]] =
    sql"""
         SELECT u.tg_chat_id
         FROM organization_member om
         INNER JOIN "user" u ON u.id = om.user_id
         WHERE om.id = $id
       """.query[TgChatId].option.transact(transactor)

  override def getOrganizationMemberByUserId(
      userId: UserId,
      organizationId: OrganizationId
  ): F[Option[OrganizationMember]] =
    sql"""
         SELECT id, user_id, organization_id, ad_tenant_id, name, email, phone_number, is_active, created_at, updated_at
         FROM organization_member
         WHERE user_id = $userId AND organization_id = $organizationId
     """.query[OrganizationMember].option.transact(transactor)
}
