package dev.kovstas.adsyncbot.telegram

import canoe.api.{Scenario, TelegramClient}
import canoe.models.PrivateChat
import canoe.models.messages.UserMessage
import cats.effect.IO

class ChatScenario(telegramClient: TelegramClient[IO]) {

  def checkUserPermissions() = {
    Scenario.expect {
      case m: UserMessage if m.chat.asInstanceOf[PrivateChat]
    }
  }



}
