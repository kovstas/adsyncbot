package dev.kovstas.adsyncbot.user

import cats.effect.IO
import dev.kovstas.adsyncbot.model.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId

trait UserRepository[F[_]] {
  def upsertUser(
      nickName: String,
      tgChatId: TgChatId
  ): F[UserId]
}

final class PgUserRepository extends UserRepository[IO] {

  override def upsertUser(
      nickName: String,
      tgChatId: TgChatId
  ): IO[UserId] = ???
}
