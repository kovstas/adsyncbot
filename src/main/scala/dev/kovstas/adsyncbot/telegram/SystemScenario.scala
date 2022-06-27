package dev.kovstas.adsyncbot.telegram

import canoe.api.Scenario
import canoe.models.messages.{ChatMemberAdded, ChatMemberLeft}
import canoe.models.{Group, User}
import dev.kovstas.adsyncbot.chat.ChatService

final class SystemScenario[F[_]](chatService: ChatService[F]) {

  def system: Scenario[F, Unit] = {
    Scenario
      .expect {
        case ChatMemberAdded(_, chat: Group, _, newChatMembers: Seq[User]) =>
          chatService.addMembers(
            chat: Group,
            newChatMembers.toList.filterNot(_.isBot)
          )
        case ChatMemberLeft(_, chat: Group, _, user: User) =>
          chatService.deleteMember(TgChatId(chat.id), TgChatId(user.id))
      }
      .flatMap(Scenario.eval)

  }

}
