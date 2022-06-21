package dev.kovstas.adsyncbot.user

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import dev.kovstas.adsyncbot.model.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId
import java.time.Instant
import java.util.UUID

trait UserService[F[_]] {
  def findUserByChat(tgUserId: TgChatId): F[Option[User]]
  def findUserById(id: UserId): F[Option[User]]
  def createUser(
      name: Option[String],
      tgChatId: TgChatId
  ): F[UserId]
  def actualiseUserInfo(
      userId: UserId,
      name: String,
      tgChatId: TgChatId
  ): F[Unit] // should activate user
}

object MockUserService extends UserService[IO] {
  override def findUserByChat(tgUserId: TgChatId): IO[Option[User]] =
    None.pure[IO]

  override def findUserById(id: UserId): IO[Option[User]] = Some(
    User(
      id,
      "test",
      TgChatId(83020144),
      isActive = true,
      Instant.now(),
      Instant.now()
    )
  ).pure[IO]

  override def createUser(
      name: Option[String],
      tgChatId: TgChatId
  ): IO[UserId] = IO(UserId(UUID.randomUUID()))

  override def actualiseUserInfo(
      userId: UserId,
      name: String,
      tgChatId: TgChatId
  ): IO[Unit] = IO.unit
}
