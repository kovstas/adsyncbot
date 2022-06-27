package dev.kovstas.adsyncbot.telegram

import canoe.api._
import canoe.models._
import canoe.syntax._
import dev.kovstas.adsyncbot.chat.ChatService
import dev.kovstas.adsyncbot.user.UserService

final class StartScenario[F[_]: TelegramClient](
    userService: UserService[F],
    chatService: ChatService[F]
) {

  def start: Scenario[F, Unit] =
    for {
      msg <- Scenario.expect(command("start"))
      _ <- Scenario.eval(msg.chat match {
        case p: PrivateChat => userService.create(p)
        case g: Group       => chatService.create(g)
        case any =>
          any
            .send(
              "This type of chat isn't supported. Please try using a private chat or a group either."
            )
      })
    } yield ()

}
