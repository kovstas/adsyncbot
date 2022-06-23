package dev.kovstas.adsyncbot.auth

import canoe.api._
import canoe.api.models.Keyboard.Inline
import canoe.models.InlineKeyboardMarkup
import canoe.syntax._
import cats.effect.IO
import dev.kovstas.adsyncbot.config.MsAuthConfig
import dev.kovstas.adsyncbot.organization.Organization.ADTenantId
import dev.kovstas.adsyncbot.organization.OrganizationService
import dev.kovstas.adsyncbot.telegram.loginAsOrganizationMemberButton
import dev.kovstas.adsyncbot.user.User.UserId
import dev.kovstas.adsyncbot.user.UserService
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait MsAuthService[F[_]] {

  def organizationLogin(
      adminConsent: Boolean,
      tenantId: ADTenantId,
      userId: UserId
  ): F[Unit]

  def organizationMemberLogin(
      accessToken: String,
      userId: UserId
  ): F[Unit]

}

final class DefaultMsAuthService(
    msAuthClient: MsAuthClient[IO],
    telegramClient: TelegramClient[IO],
    organizationService: OrganizationService[IO],
    userService: UserService[IO],
    msAuthConfig: MsAuthConfig
) extends MsAuthService[IO] {

  override def organizationMemberLogin(
      accessToken: String,
      userId: UserId
  ): IO[Unit] = {
    for {
      logger <- Slf4jLogger
        .create[IO]
        .map(_.addContext(Map("userId" -> userId.value.toString)))

      userChat <- userService.getUserChat(userId)
      r <- organizationService.addMember(userId, accessToken).attempt
      _ <- r match {
        case Right((orgName, _)) =>
          logger.debug(s"Organization member was added to $orgName") *>
            userChat.send(
              s"You successfully connected with $orgName organization. "
            )(telegramClient)
        case Left(e @ OrganizationNotFound(name)) =>
          logger.warn(e)(e.getMessage) *>
            userChat.send(
              s"I couldn't find your organization $name. Please, ask administrator of your organization to setup synchronization with the bot first."
            )(telegramClient)
        case Left(e) =>
          logger.error(e)(e.getMessage) *>
            userChat.send(
              s"Sorry, I couldn't connect you with your organization due to an unknown error"
            )(telegramClient)
      }

    } yield ()

  }

  override def organizationLogin(
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

      userChat <- userService.getUserChat(userId)
      result <- msAuthClient.loginOrganization(tenantId).attempt
      _ <- result.fold(
        e =>
          logger.warn(e.getMessage) *> userChat
            .send(
              "Can't login to your organization. Try to check your permissions. You should be the admin of your organization."
            )(telegramClient)
            .void,
        token =>
          organizationService
            .create(userId, tenantId, token.accessToken)
            .attempt
            .flatMap {
              case Right(name) =>
                userChat.send(
                  s"Setup AD synchronization of '$name' organization is finished. Please log in as a organisation member.",
                  keyboard = Inline(
                    InlineKeyboardMarkup.singleButton(
                      loginAsOrganizationMemberButton(
                        msAuthConfig,
                        userId
                      )
                    )
                  )
                )(telegramClient)
              case Left(ex) =>
                val msg = "The organisation can't be synchronized."
                logger.error(ex)(s"$msg ${ex.getMessage}") *> userChat.send(
                  msg
                )(telegramClient)

            }
      )
    } yield ()
  }

}
