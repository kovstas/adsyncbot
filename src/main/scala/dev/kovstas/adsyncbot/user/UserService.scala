package dev.kovstas.adsyncbot.user

import canoe.api._
import canoe.api.models.Keyboard.Inline
import canoe.models.{
  Chat,
  InlineKeyboardButton,
  InlineKeyboardMarkup,
  PrivateChat
}
import canoe.syntax._
import cats.MonadThrow
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.OAuthHelper.organizationAuthoriseLink
import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.organization.OrganizationRepo
import dev.kovstas.adsyncbot.telegram.{
  TgChatId,
  UnknownErrorMessage,
  loginAsOrganizationMemberButton
}
import dev.kovstas.adsyncbot.user.User.UserId
import org.typelevel.log4cats.StructuredLogger
import scala.util.control.NonFatal

trait UserService[F[_]] {
  def create(chat: PrivateChat): F[Unit]
}

final class DefaultUserService[F[
    _
]: MonadThrow: TelegramClient: StructuredLogger](
    userRepo: UserRepo[F],
    organizationRepo: OrganizationRepo[F],
    msConfig: MsConfig
) extends UserService[F] {

  override def create(chat: PrivateChat): F[Unit] = {
    val tgUserId = TgChatId(chat.id)
    val logger =
      StructuredLogger[F].addContext(Map("chatId" -> chat.id.toString))

    (for {
      userOpt <- userRepo.userByTgUserId(tgUserId)
      userState <- userOpt.traverse { user =>
        for {
          _ <- userRepo.activateUser(user.id)
          orgOpt <- organizationRepo.getOrganizationByUserId(user.id)
          memberOpt <- orgOpt.flatTraverse(org =>
            organizationRepo.getOrganizationMemberByUserId(user.id, org.id)
          )
        } yield (user, orgOpt, memberOpt)
      }
      _ <- userState match {
        case Some((user, None, None)) =>
          sendHelloMsg(chat, user.id)
        case Some((user, Some(organization), Some(member))) =>
          if (organization.isActive) {
            if (member.isActive) {
              if (organization.createdBy == user.id)
                chat.send(
                  s"You've already synchronized your ${organization.name} organization. Now you can add the bot to your telegram chats."
                )
              else
                chat.send(
                  s"You've already connected to ${organization.name} organization. You can ask the admin of organization to add you to organization telegram chats."
                )
            } else {
              chat.send(
                s"Your AD user was deactivated. Please, try logging in as the member of ${organization.name} organization",
                keyboard = Inline(
                  InlineKeyboardMarkup.singleButton(
                    loginAsOrganizationMemberButton(
                      msConfig,
                      user.id
                    )
                  )
                )
              )
            }
          } else {
            sendHelloMsg(chat, user.id)
          }

        case Some((user, Some(_), None)) =>
          chat
            .send(
              s"Please log in as a organization member.",
              keyboard = Inline(
                InlineKeyboardMarkup.singleButton(
                  loginAsOrganizationMemberButton(
                    msConfig,
                    user.id
                  )
                )
              )
            )
        case _ =>
          for {
            userId <- userRepo.createUser(tgUserId, chat.username)
            _ <- logger.debug(s"User with id ${userId} was created")
            _ <- sendHelloMsg(chat, userId)
          } yield ()
      }
    } yield ()).recoverWith { case NonFatal(ex) =>
      logger.error(ex)("Exception during creation a user") <*
        chat.send(UnknownErrorMessage)
    }

  }

  private def sendHelloMsg(chat: Chat, userId: UserId): F[Unit] =
    chat
      .send(
        "Hello. I'm a AD synchronizer bot. I will help you either to synchronize your AD organization or sign in as a organization member. What can I do for you?",
        keyboard = Inline(
          InlineKeyboardMarkup.singleColumn(
            Seq(
              InlineKeyboardButton.url(
                "Setup AD synchronisation",
                organizationAuthoriseLink(
                  msConfig,
                  userId
                ).renderString
              ),
              loginAsOrganizationMemberButton(
                msConfig,
                userId
              )
            )
          )
        )
      )
      .void

}
