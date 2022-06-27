package dev.kovstas.adsyncbot.user

import cats.effect.MonadCancelThrow
import cats.implicits.toFunctorOps
import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId
import doobie.implicits._
import doobie.postgres.implicits._
import doobie._
import java.time.Clock
import java.util.UUID

trait UserRepo[F[_]] {

  def userByTgUserId(tgUserId: TgChatId): F[Option[User]]

  def userById(userId: UserId): F[Option[User]]

  def activateUser(
      userId: UserId
  ): F[Unit]

  def createUser(tgChatId: TgChatId, name: Option[String]): F[UserId]
}

final class PostgresUserRepo[F[_]: MonadCancelThrow](
    clock: Clock,
    transactor: Transactor[F]
) extends UserRepo[F] {

  override def userByTgUserId(tgUserId: TgChatId): F[Option[User]] =
    sql"""SELECT id, name, tg_chat_id, is_active, created_at, updated_at FROM "user" WHERE tg_chat_id = $tgUserId"""
      .query[User]
      .option
      .transact(transactor)

  override def userById(userId: UserId): F[Option[User]] =
    sql"""SELECT id, name, tg_chat_id, is_active, created_at, updated_at FROM "user" WHERE id = $userId"""
      .query[User]
      .option
      .transact(transactor)

  override def activateUser(userId: UserId): F[Unit] = {
    val now = clock.instant()
    sql"""UPDATE "user" SET is_active = true, updated_at = $now WHERE id = $userId""".update.run
      .transact(transactor)
      .void
  }

  override def createUser(
      tgChatId: TgChatId,
      name: Option[String]
  ): F[UserId] = {
    val now = clock.instant()
    val id = UserId(UUID.randomUUID())
    sql"""
        INSERT INTO "user" (id, name, tg_chat_id, is_active, created_at, updated_at)
        VALUES ($id, $name, $tgChatId, true, $now, $now) 
       """.update.run.transact(transactor).as(id)
  }
}
