package dev.kovstas.adsyncbot.telegram

import canoe.api.{Scenario, TelegramClient}
import canoe.models.{Chat, User}
import canoe.models.messages.{ChatMemberAdded, ChatMemberLeft, GroupChatCreated}
import cats.effect.IO

class SystemScenarios(telegramClient: TelegramClient[IO]) {

  def system = {
    Scenario.expect {
      case ChatMemberAdded(_, chat: Chat, _, newChatMembers: Seq[User]) =>
        // restrict user permission
        // check if user signed, ask user to sign through bot
        // allow user to do smth
      case ChatMemberLeft(_, chat: Chat, _, user: User) => ??? // delete member, make user inactive
      case GroupChatCreated(_, chat: Chat, _, _) => ??? // try to connect group to organization
    }

  }

}
