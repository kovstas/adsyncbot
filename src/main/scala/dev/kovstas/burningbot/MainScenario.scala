package dev.kovstas.burningbot

import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.{
  CallbackButtonSelected,
  Group,
  InlineKeyboardButton,
  InlineKeyboardMarkup,
  PrivateChat,
  Update,
  User
}
import canoe.syntax._
import cats.{Applicative, Monad}
import dev.kovstas.burningbot.teamMember.TeamMemberRepo
import fs2.Pipe
import io.estatico.newtype.ops._
import cats.syntax.all._
import dev.kovstas.burningbot.config.MsAuthConfig
import dev.kovstas.burningbot.model.TeamMember.TgUserId

final class MainScenario[F[_]: TelegramClient: Monad](
    teamMemberRepo: TeamMemberRepo[F],
    msAuthConfig: MsAuthConfig
) {

  def start: Scenario[F, Unit] =
    Scenario.expect(command("start")).flatMap { msg =>
      msg.chat match {
        case p: PrivateChat =>
          privateChat(p, msg.from.get)
        case _: Group => ??? //groupChat(g)
        case chat =>
          Scenario.eval(
            chat
              .send(
                "This type of chat isn't supported. Please try using a private chat or a group."
              )
              .void
          )
      }
    }

  val teamCBD = "team"
  val teamMemberCDB = "member"

  def privateChat(
      privateChat: PrivateChat,
      user: User
  ): Scenario[F, Unit] = {
    for {
      userOpt <- Scenario.eval(
        teamMemberRepo.getUserByTgId(user.id.coerce[TgUserId])
      )
      _ <- userOpt match {
        case Some(_) => ???
        case None =>
          Scenario.eval(
            privateChat.send(
              "Hi. This is the burnerapp bot. What do you want to do?",
              keyboard = Keyboard
                .Inline(
                  InlineKeyboardMarkup.singleRow(
                    List(
                      InlineKeyboardButton.callbackData("Create Team", teamCBD),
                      InlineKeyboardButton.callbackData(
                        "Login as team member",
                        teamMemberCDB
                      )
                    )
                  )
                )
            )
          )
      }

    } yield ()
  }

//  def groupChat(group: Group): Scenario[F, Unit] =
//    ???

  def answerCallbacks: Pipe[F, Update, Update] =
    _.evalTap {
      case CallbackButtonSelected(_, query) =>
        query.data match {
          case Some(cbd) if cbd === teamCBD =>
            for {
              _ <- query.message.traverse { msg =>
                msg.chat.send(
                  s"Please auth your through this link: ${msAuthConfig.authoriseLink}"
                )
              }
              _ <- query.finish
            } yield ()
          case _ => Applicative[F].unit
        }
      case _ => Applicative[F].unit
    }

}
