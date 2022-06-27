package dev.kovstas.adsyncbot.resources

import canoe.api.TelegramClient
import cats.effect.Spawn
import cats.effect.kernel.{Resource, Temporal}
import dev.kovstas.adsyncbot.organization.OrganizationProcess
import dev.kovstas.adsyncbot.telegram.TelegramProcess
import org.typelevel.log4cats.Logger

object Processes {

  def apply[F[_]: Temporal: TelegramClient: Logger](
      appServices: AppServices[F]
  ): Resource[F, Unit] = {
    val tgProcess = new TelegramProcess[F](
      appServices.userService,
      appServices.chatService
    )

    val checkOrgProcess = new OrganizationProcess[F](
      appServices.organizationService
    )

    for {
      _ <- Spawn[F].background(checkOrgProcess.process.compile.drain)
      _ <- Spawn[F].background(tgProcess.process.compile.drain)
    } yield ()

  }

}
