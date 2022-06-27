package dev.kovstas.adsyncbot.telegram

import canoe.api.{Bot, TelegramClient}
import canoe.models.Update
import cats.effect.kernel.Concurrent
import dev.kovstas.adsyncbot.chat.ChatService
import dev.kovstas.adsyncbot.user.UserService

final class TelegramProcess[F[_]: Concurrent: TelegramClient](
    userService: UserService[F],
    chatService: ChatService[F]
) {

  val process: fs2.Stream[F, Update] = {

    val startScenario = new StartScenario[F](
      userService,
      chatService
    )

    val systemScenario = new SystemScenario[F](chatService)

    Bot
      .polling[F]
      .follow(startScenario.start, systemScenario.system)
  }
}
