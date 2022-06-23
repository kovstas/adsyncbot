package dev.kovstas.adsyncbot.chat

import canoe.models.{Group => TgGroup}
import dev.kovstas.adsyncbot.chat.Chat.ChatId
import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId

trait ChatService[F[_]] {
  def findChat(tgChatId: TgChatId): F[Option[Chat]]
  def create(tgGroup: TgGroup): F[Unit]

}
