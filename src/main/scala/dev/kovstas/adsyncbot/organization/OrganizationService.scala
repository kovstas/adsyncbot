package dev.kovstas.adsyncbot.organization

import canoe.api.{TelegramClient, chatApi}
import canoe.models.PrivateChat
import canoe.syntax._
import cats.MonadThrow
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.OrganizationNotFound
import dev.kovstas.adsyncbot.az.{ApplicationGraphApi, UserGraphApi}
import dev.kovstas.adsyncbot.chat.ChatService
import dev.kovstas.adsyncbot.organization.Organization.AdTenantId
import dev.kovstas.adsyncbot.user.User.UserId
import org.typelevel.log4cats.StructuredLogger

trait OrganizationService[F[_]] {
  def addOrganizationMember(
      userId: UserId,
      accessToken: String
  ): F[(String, Boolean)]

  def createOrganization(
      userId: UserId,
      tenantId: AdTenantId
  ): F[String]

  def checkOrganizationState: F[Unit]
}

final class DefaultOrganizationService[
    F[_]: MonadThrow: TelegramClient: StructuredLogger
](
    applicationGraphApi: ApplicationGraphApi[F],
    userGraphApi: UserGraphApi[F],
    organizationRepo: OrganizationRepo[F],
    chatService: ChatService[F]
) extends OrganizationService[F] {

  override def createOrganization(
      userId: UserId,
      tenantId: AdTenantId
  ): F[String] = {
    val logger = StructuredLogger[F].addContext(
      "userId" -> userId.value,
      "tenantId" -> tenantId.value
    )
    for {
      _ <- logger.debug("Start creating organization...")
      orgInfo <- applicationGraphApi.organization(tenantId)
      id <- organizationRepo.insert(tenantId, orgInfo.displayName, userId)
      _ <- logger.debug(s"Organization '$id' successfully created")
    } yield orgInfo.displayName
  }

  override def addOrganizationMember(
      userId: UserId,
      accessToken: String
  ): F[(String, Boolean)] = {
    (
      userGraphApi.me(accessToken),
      userGraphApi.organization(accessToken)
    ).mapN { case (userInfo, orgInfo) =>
      for {
        orgOpt <- organizationRepo.getOrganization(orgInfo.id)
        memberIsOwner <- orgOpt.fold(
          MonadThrow[F].raiseError[Boolean](
            OrganizationNotFound(orgInfo.displayName)
          )
        ) { org =>
          organizationRepo
            .insertMember(
              organizationId = org.id,
              userId = userId,
              adUserId = userInfo.id,
              email = userInfo.mail,
              name = userInfo.displayName,
              phoneNumber = userInfo.businessPhones.headOption
            )
            .as(org.createdBy == userId)
        }
      } yield (orgInfo.displayName, memberIsOwner)

    }.flatten

  }

  override def checkOrganizationState: F[Unit] = {
    for {
      _ <- StructuredLogger[F].debug("Start checking organizations...")
      organizations <- organizationRepo.getActiveOrganizations
      members <- organizations.flatTraverse(o =>
        organizationRepo
          .getActiveOrganizationMembers(o.id)
          .map(_.map((_, o.name, o.adTenantId)))
      )
      _ <- members.traverse { case (orgMember, name, orgTenantId) =>
        val logger = StructuredLogger[F].addContext(
          "userId" -> orgMember.userId.value.toString,
          "organizationId" -> orgMember.organizationId.value.toString,
          "organizationMemberId" -> orgMember.id.value.toString
        )
        applicationGraphApi.user(orgTenantId, orgMember.adUserId).flatMap {
          case Some(user) if user.accountEnabled =>
            logger.debug(s"User info was updated.") // TODO: updating info
          case _ =>
            for {
              _ <- organizationRepo.deactivateMember(orgMember.id)
              tgPrivateChat <- organizationRepo.getTgMemberId(orgMember.id)
              _ <- chatService.kickUser(orgMember.organizationId, orgMember.id)
              _ <- tgPrivateChat.traverse(id =>
                PrivateChat(id.value, None, None, None).send(
                  s"You was deactivated in '$name' organization."
                )
              )
              _ <- logger.debug(s"User was deactivated.")
            } yield ()
        }
      }
      _ <- StructuredLogger[F].debug("Finish checking organizations")
    } yield ()

  }
}
