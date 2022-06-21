package dev.kovstas.adsyncbot.auth

import canoe.api._
import canoe.models.PrivateChat
import canoe.syntax._
import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import dev.kovstas.adsyncbot.company.CompanyService
import dev.kovstas.adsyncbot.model.Company.ADTenantId
import dev.kovstas.adsyncbot.user.User.UserId
import dev.kovstas.adsyncbot.user.UserService
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait MsAuthService[F[_]] {
  def companyLogin(
      adminConsent: Boolean,
      tenantId: ADTenantId,
      userId: UserId
  ): F[Unit]
}

final class DefaultMsAuthService(
    msAuthClient: MsAuthClient[IO],
    telegramClient: TelegramClient[IO],
    companyService: CompanyService[IO],
    userService: UserService[IO]
) extends MsAuthService[IO] {

  override def companyLogin(
      adminConsent: Boolean,
      tenantId: ADTenantId,
      userId: UserId
  ): IO[Unit] = {
    for {
      logger <- Slf4jLogger
        .create[IO]
        .map(
          _.addContext("userId" -> userId.value, "tenantId" -> tenantId.value)
        )
      user <- userService.findUserById(userId).flatMap {
        case Some(user) => user.pure[IO]
        case None =>
          val e = UserNotFound(userId)
          logger.error(e.getMessage) *> IO.raiseError(e)
      }
      tgUserChat = PrivateChat(user.tgChatId.value, None, None, None)

      result <- msAuthClient.loginCompany(tenantId).attempt
      _ <- result.fold(
        e =>
          logger.warn(e.getMessage) *> tgUserChat
            .send(
              "Can't login to your company. Try to check your permissions. You should be the admin of AD."
            )(telegramClient)
            .void,
        token =>
          companyService
            .createCompany(userId, tenantId, token.accessToken) <* tgUserChat
            .send(
              "Company has been created."
            )(telegramClient)
      )
    } yield ()
  }

}
