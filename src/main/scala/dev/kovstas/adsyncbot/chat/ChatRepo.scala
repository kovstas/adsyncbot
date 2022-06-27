package dev.kovstas.adsyncbot.chat

import cats.effect.MonadCancelThrow
import cats.implicits.toFunctorOps
import dev.kovstas.adsyncbot.chat.Chat.ChatId
import dev.kovstas.adsyncbot.organization.Organization.OrganizationId
import dev.kovstas.adsyncbot.organization.OrganizationMember.OrganizationMemberId
import dev.kovstas.adsyncbot.telegram.TgChatId
import doobie.Transactor
import doobie.postgres.implicits._
import doobie.implicits._
import java.time.Clock
import java.util.UUID

trait ChatRepo[F[_]] {
  def getGroupChat(id: TgChatId): F[Option[Chat]]
  def getChatsByOrganizationId(organizationId: OrganizationId): F[List[Chat]]

  def deleteMember(
      chatId: ChatId,
      organizationMemberId: OrganizationMemberId
  ): F[Unit]

  def createChat(
      tgChatId: TgChatId,
      organizationId: OrganizationId,
      name: Option[String]
  ): F[ChatId]

  def addMember(
      chatId: ChatId,
      organizationMemberId: OrganizationMemberId,
      role: ChatMemberRole
  ): F[Unit]
}

final class PostgresChatRepo[F[_]: MonadCancelThrow](
    clock: Clock,
    transactor: Transactor[F]
) extends ChatRepo[F] {
  override def getGroupChat(id: TgChatId): F[Option[Chat]] =
    sql"SELECT id, tg_chat_id, organization_id, name, created_at, updated_at FROM chat WHERE tg_chat_id = $id"
      .query[Chat]
      .option
      .transact(transactor)

  override def getChatsByOrganizationId(
      organizationId: OrganizationId
  ): F[List[Chat]] =
    sql"SELECT id, tg_chat_id, organization_id, name, created_at, updated_at FROM chat WHERE organization_id = $organizationId"
      .query[Chat]
      .to[List]
      .transact(transactor)

  override def deleteMember(
      chatId: ChatId,
      organizationMemberId: OrganizationMemberId
  ): F[Unit] =
    sql"DELETE FROM chat_member WHERE chat_id = $chatId AND organization_member_id = $organizationMemberId".update.run
      .transact(transactor)
      .void

  override def createChat(
      tgChatId: TgChatId,
      organizationId: OrganizationId,
      name: Option[String]
  ): F[ChatId] = {
    val now = clock.instant()
    val id = ChatId(UUID.randomUUID())

    sql"""
        INSERT INTO chat (id, tg_chat_id, organization_id, name, created_at, updated_at)
        VALUES ($id, $tgChatId, $organizationId, $name, $now, $now)
    """.update.run.transact(transactor).as(id)
  }

  override def addMember(
      chatId: ChatId,
      organizationMemberId: OrganizationMemberId,
      role: ChatMemberRole
  ): F[Unit] = {
    val now = clock.instant()
    sql"""
        INSERT INTO chat_member (chat_id, organization_member_id, role, created_at, updated_at)
        VALUES ($chatId, $organizationMemberId, $role, $now, $now)
       """.update.run.transact(transactor).void
  }
}
