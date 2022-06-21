package dev.kovstas.adsyncbot

import canoe.api._
import canoe.api.models.Keyboard.Inline
import canoe.models.{
  Group,
  InlineKeyboardButton,
  InlineKeyboardMarkup,
  PrivateChat
}
import canoe.syntax._
import cats.effect.kernel.Sync
import cats.syntax.all._
import dev.kovstas.adsyncbot.config.MsAuthConfig
import dev.kovstas.adsyncbot.model.TgChatId
import dev.kovstas.adsyncbot.user.UserService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

final class MainScenario[F[_]: TelegramClient: Sync](
    userService: UserService[F],
    //companyService: CompanyService[F],
    msAuthConfig: MsAuthConfig
) {

  private val logger: Logger[F] = Slf4jLogger.getLogger[F]

  def start: Scenario[F, Unit] =
    Scenario.expect(command("start")).flatMap { msg =>
      msg.chat match {
        case p: PrivateChat =>
          Scenario.eval {
            userService
              .findUserByChat(TgChatId(p.id))
              .flatMap {
                case Some(_) => ???
                //                (
                //                  userService
                //                    .actualiseUserInfo(user.id, user.name, TgChatId(p.id)),
                //                  companyService.findCompanyByUserId(user.id),
                //                  companyService.findCompanyUserByUserId(user.id)
                //                ).parMapN { case (_, c, t) =>
                //                  ???
                //                }
                case None =>
                  userService
                    .createUser(
                      p.username,
                      TgChatId(p.id)
                    )
                    .flatTap(userId =>
                      logger.debug(
                        s"User was created with id $userId, chatId ${p.id}"
                      )
                    )
                    .flatMap { userId =>
                      p.send(
                        "Hello. I'm a AD synchonizer bot",
                        keyboard = Inline(
                          InlineKeyboardMarkup.singleColumn(
                            Seq(
                              InlineKeyboardButton.url(
                                "Setup AD synchronisation",
                                msAuthConfig
                                  .authoriseLink(userId)
                                  .renderString
                              ),
                              InlineKeyboardButton.url(
                                "Connect as a company member",
                                msAuthConfig.host.renderString // TODO: user auth link
                              )
                            )
                          )
                        )
                      ).void
                    }

              }
              .void
          }

        case _: Group => ??? //groupChat(g)
        case chat =>
          Scenario.eval(
            chat
              .send(
                "This type of chat isn't supported. Please try using either private chat or group."
              )
              .void
          )
      }
    }

}
