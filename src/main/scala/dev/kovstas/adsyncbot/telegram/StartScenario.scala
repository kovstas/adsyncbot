package dev.kovstas.adsyncbot.telegram

import canoe.api._
import canoe.api.models.Keyboard.Inline
import canoe.models._
import canoe.syntax.{command, _}
import cats.effect.kernel.Sync
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.OAuthHelper.companyAuthoriseLink
import dev.kovstas.adsyncbot.chat.ChatService
import dev.kovstas.adsyncbot.config.MsAuthConfig
import dev.kovstas.adsyncbot.user.User.UserId
import dev.kovstas.adsyncbot.user.{UserService, UserStateInfo}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class StartScenario[F[_]: TelegramClient: Sync](
    userService: UserService[F],
    chatService: ChatService[F],
    msAuthConfig: MsAuthConfig
) {

  def start: Scenario[F, Unit] =
    for {
      msg <- Scenario.expect(command("start"))
      logger <- Scenario.eval(
        Slf4jLogger
          .create[F]
          .map(_.addContext(Map("chatId" -> msg.chat.id.toString)))
      )
      _ <- Scenario.eval(msg.chat match {
        case p: PrivateChat => privateChatStart(p, logger)
        case g: Group       => groupStart(g)
        case any =>
          any
            .send(
              "This type of chat isn't supported. Please try using a private chat or a group either."
            )
            .void
      })
    } yield ()

  def privateChatStart(
      chat: PrivateChat,
      logger: SelfAwareStructuredLogger[F]
  ): F[Unit] = {
    userService
      .activateAndGetUserState(TgChatId(chat.id))
      .flatMap {
        case Some(UserStateInfo(user, None)) =>
          sendHelloMsg(chat, user.id)
        case Some(UserStateInfo(user, Some(organization))) =>
          if (organization.createdBy == user.id)
            chat.send(
              s"You've already synchronized your ${organization.name} organization."
            )
          else {
            // TODO: handle inactive organization
            chat.send(
              s"You've already connected to ${organization.name} organization."
            )
          }
        case None => createNewUser(chat)(logger)
      }
      .void
  }

  def groupStart(
      tgChat: Group
  ): F[Unit] = {
    val tgChatId = TgChatId(tgChat.id)
    chatService.findChat(tgChatId).flatMap {
      case Some(chat) =>
        if (chat.organizationId.isEmpty) {}
        tgChat
          .send(
            "The chat is already connected to your organization."
          )
          .void
      case None =>
        chatService
          .create(tgChat)
          .flatMap(_ =>
            tgChat
              .send(
                "The chat was successfully connected to organization."
              )
          )
          .void
    }
  }

  private def createNewUser(
      chat: PrivateChat
  )(logger: SelfAwareStructuredLogger[F]) = {
    for {
      userId <- userService.createUser(chat.username, TgChatId(chat.id))
      _ <- logger.debug(s"User with id ${userId} was created")
      _ <- sendHelloMsg(chat, userId)
    } yield ()
  }

  private def sendHelloMsg(chat: Chat, userId: UserId): F[Unit] =
    chat
      .send(
        "Hello. I'm a AD synchronizer bot. ",
        keyboard = Inline(
          InlineKeyboardMarkup.singleColumn(
            Seq(
              InlineKeyboardButton.url(
                "Setup AD synchronisation",
                companyAuthoriseLink(
                  msAuthConfig,
                  userId
                ).renderString
              ),
              loginAsOrganizationMemberButton(
                msAuthConfig,
                userId
              )
            )
          )
        )
      )
      .void

}
