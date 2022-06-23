package dev.kovstas.adsyncbot.user

import cats.effect.IO
import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId

trait UserRepo[F[_]] {

  def userByChatId(chatId: TgChatId): F[Option[User]]
  def userById(userId: UserId): F[Option[User]]

  def activateUser(
      tgChatId: TgChatId
  ): F[UserId]
}

final class PgUserRepo extends UserRepo[IO] {

  override def upsertUser(
      nickName: String,
      tgChatId: TgChatId
  ): IO[UserId] = ???
}
