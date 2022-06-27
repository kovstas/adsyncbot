package dev.kovstas.adsyncbot.chat

import canoe.api._
import canoe.models.{
  ChatAdministrator,
  ChatCreator,
  PrivateChat,
  Group => TgGroup,
  User => TgUser
}
import canoe.syntax._
import cats.MonadThrow
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.{UserInActive, UserNotFound}
import dev.kovstas.adsyncbot.organization.Organization.OrganizationId
import dev.kovstas.adsyncbot.organization.OrganizationMember.OrganizationMemberId
import dev.kovstas.adsyncbot.organization.OrganizationRepo
import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId
import dev.kovstas.adsyncbot.user.UserRepo
import org.typelevel.log4cats.StructuredLogger
import scala.util.control.NonFatal

trait ChatService[F[_]] {
  def create(tgGroup: TgGroup): F[Unit]
  def kickUser(
      organizationId: OrganizationId,
      organizationMemberId: OrganizationMemberId
  ): F[Unit]
  def getTgUserChat(userId: UserId): F[PrivateChat]
  def addMembers(tgGroup: TgGroup, members: List[TgUser]): F[Unit]
  def deleteMember(tgChatId: TgChatId, tgUserId: TgChatId): F[Unit]

}

final class DefaultChatService[F[
    _
]: TelegramClient: StructuredLogger: MonadThrow](
    organizationRepo: OrganizationRepo[F],
    userRepo: UserRepo[F],
    chatRepo: ChatRepo[F]
) extends ChatService[F] {

  override def deleteMember(tgChatId: TgChatId, tgUserId: TgChatId): F[Unit] = {
    val logger =
      StructuredLogger[F].addContext(Map("chatId" -> tgChatId.toString))
    (
      chatRepo.getGroupChat(tgChatId),
      organizationRepo.getOrganizationMemberByTgUserId(tgUserId)
    ).mapN {
      case (Some(chat), Some(orgMember)) =>
        chatRepo.deleteMember(chat.id, orgMember.id) *>
          logger.debug(s"Chat member with id ${orgMember.userId} was deleted")
      case _ =>
        logger.warn(
          "The chat member can't be deleted, because it isn't a part of organization or the chat wasn't activated"
        )
    }.flatten
  }

  override def addMembers(
      tgGroup: TgGroup,
      members: List[TgUser]
  ): F[Unit] = {
    val tgChatId = TgChatId(tgGroup.id)
    val logger =
      StructuredLogger[F].addContext(Map("chatId" -> tgChatId.toString))

    chatRepo
      .getGroupChat(tgChatId)
      .flatMap {
        _.traverse { chat =>
          members.traverse { tgUser =>
            val tgUserId = TgChatId(tgUser.id)
            organizationRepo.getOrganizationMemberByTgUserId(tgUserId).flatMap {
              case Some(orgMember)
                  if orgMember.isActive && chat.organizationId == orgMember.organizationId =>
                chatRepo
                  .addMember(chat.id, orgMember.id, ChatMemberRole.Member) *>
                  logger
                    .debug(s"User ${orgMember.userId} was added to the chat.")
              case _ =>
                tgGroup.send(
                  s"The user ${tgUser.firstName} doesn't have an access to this chat."
                ) *>
                  tgGroup.kickUser(tgUser.id).void
            }
          }
        }
      }
      .void
  }

  override def create(tgGroup: TgGroup): F[Unit] = {
    val tgGroupId = TgChatId(tgGroup.id)
    val logger =
      StructuredLogger[F].addContext(Map("chatId" -> tgGroup.id.toString))

    chatRepo
      .getGroupChat(tgGroupId)
      .flatMap {
        case Some(_) =>
          tgGroup
            .send(
              "The chat is already connected to your organization."
            )
            .void
        case None =>
          tgGroup.administrators.flatMap { members =>
            val tgOwnerIdOpt: Option[TgChatId] =
              members.collectFirst {
                case owner: ChatCreator if !owner.user.isBot =>
                  TgChatId(owner.user.id)
              }

            val tgAdminsChatId =
              members.collect {
                case admin: ChatAdministrator if !admin.user.isBot =>
                  TgChatId(admin.user.id)
              }

            tgOwnerIdOpt
              .flatTraverse(organizationRepo.getOrganizationMemberByTgUserId)
              .flatMap {
                case Some(orgMember) =>
                  val loggerOrg = logger.addContext(
                    Map(
                      "organizationId" -> orgMember.organizationId.value.toString
                    )
                  )
                  for {
                    _ <- loggerOrg.debug("Start creating chat...")
                    chatId <- chatRepo.createChat(
                      tgGroupId,
                      orgMember.organizationId,
                      tgGroup.title
                    )
                    _ <- chatRepo.addMember(
                      chatId,
                      orgMember.id,
                      ChatMemberRole.Owner
                    )
                    admins <- tgAdminsChatId.flatTraverse(id =>
                      organizationRepo
                        .getOrganizationMemberByTgUserId(id)
                        .map(_.toList)
                    )
                    _ <- admins.traverse(orgMember =>
                      chatRepo.addMember(
                        chatId,
                        orgMember.id,
                        ChatMemberRole.Admin
                      )
                    )
                    _ <- loggerOrg.debug("The chat was created")
                    _ <- tgGroup.send(
                      "The chat was successfully connected to organization."
                    )
                  } yield ()
                case None =>
                  tgGroup
                    .send(
                      "Can't connect your organization to the chat. The owner of the chat should be the owner of your organization."
                    ) *> logger.warn(
                    "Can't connect the organization to the chat"
                  )
              }

          }

      }
      .recoverWith { case NonFatal(ex) =>
        logger.error(ex)("Exception during creation a chat") <*
          tgGroup.send("Unknown error. Please, try again later.")
      }
  }

  override def kickUser(
      organizationId: OrganizationId,
      organizationMemberId: OrganizationMemberId
  ): F[Unit] = {
    for {
      chats <- chatRepo.getChatsByOrganizationId(organizationId)
      _ <- chats.traverse { chat =>
        val userGroup = TgGroup(chat.tgChatId.value, chat.name)
        val logger = StructuredLogger[F].addContext(
          Map("chatId" -> chat.id.value.toString)
        )
        organizationRepo.getTgMemberId(organizationMemberId).flatMap {
          case Some(tgChatId) =>
            for {
              _ <- userGroup.kickUser(tgChatId.value)
              _ <- chatRepo.deleteMember(chat.id, organizationMemberId)
              _ <- logger.debug("Chat's member was deleted.")
            } yield ()
          case None =>
            chatRepo.deleteMember(chat.id, organizationMemberId) *>
              logger.warn("Can't find the chat member, it's was deleted.")
        }
      }
    } yield ()

  }

  override def getTgUserChat(userId: UserId): F[PrivateChat] = {
    userRepo
      .userById(userId)
      .flatMap {
        case Some(user) if user.isActive =>
          PrivateChat(user.tgChatId.value, None, None, None).pure[F]
        case Some(_) => MonadThrow[F].raiseError(UserInActive(userId))
        case None    => MonadThrow[F].raiseError(UserNotFound(userId))
      }
  }
}
