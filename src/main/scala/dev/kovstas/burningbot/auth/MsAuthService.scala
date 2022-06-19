package dev.kovstas.burningbot.auth

import cats.effect.IO
import dev.kovstas.burningbot.model.Team.ADTenantId
import dev.kovstas.burningbot.model.TgChatId
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait MsAuthService[F[_]] {
  def tenantLogin(
      adminConsent: Boolean,
      tenantId: ADTenantId,
      tgChatId: TgChatId
  ): F[Unit]
}

final class DefaultMsAuthService(msAuthClient: MsAuthClient[IO])
    extends MsAuthService[IO] {

  private val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def tenantLogin(
      adminConsent: Boolean,
      tenantId: ADTenantId,
      tgChatId: TgChatId
  ): IO[Unit] =
    msAuthClient
      .tenantServiceToken(tenantId)
      .flatTap(r => logger.info(s"Access token received - $r"))
      .void

}
