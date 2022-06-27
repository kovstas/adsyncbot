package dev.kovstas.adsyncbot.auth

import canoe.api._
import canoe.api.models.Keyboard.Inline
import canoe.models.{InlineKeyboardMarkup, PrivateChat}
import canoe.syntax._
import cats.MonadThrow
import cats.syntax.all._
import dev.kovstas.adsyncbot.chat.ChatService
import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.organization.Organization.AdTenantId
import dev.kovstas.adsyncbot.organization.OrganizationService
import dev.kovstas.adsyncbot.telegram.loginAsOrganizationMemberButton
import dev.kovstas.adsyncbot.user.User.UserId
import org.typelevel.log4cats.{Logger, StructuredLogger}

trait OAuthService[F[_]] {

  def organizationLogin(
      adminConsent: Boolean,
      tenantId: AdTenantId,
      userId: UserId
  ): F[Unit]

  def organizationMemberLogin(
      accessToken: String,
      userId: UserId
  ): F[Unit]

}

final class DefaultOAuthService[
    F[_]: MonadThrow: TelegramClient: StructuredLogger
](
    organizationService: OrganizationService[F],
    chatService: ChatService[F],
    msConfig: MsConfig
) extends OAuthService[F] {

  override def organizationMemberLogin(
      accessToken: String,
      userId: UserId
  ): F[Unit] = {
    val logger =
      StructuredLogger[F].addContext(Map("userId" -> userId.value.toString))

    for {
      userChat <- chatService.getTgUserChat(userId)
      r <- organizationService
        .addOrganizationMember(userId, accessToken)
        .attempt
      _ <- r match {
        case Right((orgName, _)) =>
          logger.debug(s"Organization member was added to $orgName") *>
            userChat.send(
              s"You successfully connected with $orgName organization. "
            )
        case Left(e @ OrganizationNotFound(name)) =>
          logger.warn(e)(e.getMessage) *>
            userChat.send(
              s"I couldn't find your organization $name. Please, ask administrator of your organization to setup synchronization with the bot first."
            )
        case Left(e) =>
          logger.error(e)(e.getMessage) *>
            userChat.send(
              s"Sorry, I couldn't connect you with your organization due to an unknown error"
            )
      }

    } yield ()

  }

  override def organizationLogin(
      adminConsent: Boolean,
      tenantId: AdTenantId,
      userId: UserId
  ): F[Unit] = {
    chatService.getTgUserChat(userId).flatMap { chat =>
      if (adminConsent) {
        organizationService
          .createOrganization(userId, tenantId)
          .attempt
          .flatMap {
            case Right(name) =>
              chat
                .send(
                  s"Setup AD synchronization of '$name' organization is finished. Please log in as a organization member.",
                  keyboard = Inline(
                    InlineKeyboardMarkup.singleButton(
                      loginAsOrganizationMemberButton(
                        msConfig,
                        userId
                      )
                    )
                  )
                )
                .void
            case Left(ex) =>
              Logger[F].error(ex)(
                s"The organization can't be synchronized. ${ex.getMessage}"
              ) *>
                sendFailMsgOrganizationLogin(chat)
          }
      } else {
        sendFailMsgOrganizationLogin(chat)
      }
    }

  }

  private def sendFailMsgOrganizationLogin(chat: PrivateChat): F[Unit] =
    chat
      .send(
        "Can't login to your organization. Try to check your permissions. You should be the admin of your organization."
      )
      .void

}
