package dev.kovstas.adsyncbot.organization

import cats.effect.IO
import cats.syntax.all._
import dev.kovstas.adsyncbot.auth.OrganizationNotFound
import dev.kovstas.adsyncbot.az.{ApplicationGraphApi, UserGraphApi}
import dev.kovstas.adsyncbot.organization.Organization.ADTenantId
import dev.kovstas.adsyncbot.user.User.UserId
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait OrganizationService[F[_]] {
  def findByUserId(userId: UserId): F[Option[Organization]]
  def findMemberByUserId(userId: UserId): F[List[OrganizationMember]]
  def addMember(userId: UserId, accessToken: String): F[(String, Boolean)]
  def create(
      userId: UserId,
      tenantId: ADTenantId,
      accessToken: String
  ): F[String]
}

class DefaultOrganizationService(
    applicationGraphApi: ApplicationGraphApi[IO],
    userGraphApi: UserGraphApi[IO],
    organizationRepo: OrganizationRepo[IO]
) extends OrganizationService[IO] {

  override def findByUserId(userId: UserId): IO[Option[Organization]] =
    None.pure[IO]

  override def findMemberByUserId(
      userId: UserId
  ): IO[List[OrganizationMember]] =
    Nil.pure[IO]

  override def create(
      userId: UserId,
      tenantId: ADTenantId,
      accessToken: String
  ): IO[String] =
    for {
      logger <- Slf4jLogger
        .create[IO]
        .map(
          _.addContext("userId" -> userId.value, "tenantId" -> tenantId.value)
        )
      _ <- logger.debug("Start creating company...")

      orgInfo <- applicationGraphApi.organization(accessToken, tenantId)
      companyId <- organizationRepo.insert(
        tenantId,
        orgInfo.displayName,
        userId
      )
      _ <- logger.debug(
        s"Company '$companyId' successfully created"
      )

    } yield orgInfo.displayName

  override def addMember(
      userId: UserId,
      accessToken: String
  ): IO[(String, Boolean)] = {
    (
      userGraphApi.me(accessToken),
      userGraphApi.organisation(accessToken)
    ).mapN { case (userInfo, orgInfo) =>
      for {
        orgOpt <- organizationRepo.getOrganization(orgInfo.id)
        memberIsOwner <- orgOpt.fold(
          IO.raiseError[Boolean](OrganizationNotFound(orgInfo.displayName))
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
}
