package dev.kovstas.adsyncbot.user

import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class User(
    id: UserId,
    name: String,
    tgChatId: TgChatId,
    isActive: Boolean,
    createdAt: Instant,
    updatedAt: Instant
)

object User {
  @newtype case class UserId(value: UUID)
}
