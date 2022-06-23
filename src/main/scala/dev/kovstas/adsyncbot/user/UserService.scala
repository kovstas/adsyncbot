package dev.kovstas.adsyncbot.user

import canoe.models.PrivateChat
import cats.syntax.all._
import cats.effect.IO
import dev.kovstas.adsyncbot.auth.{UserInActive, UserNotFound}
import dev.kovstas.adsyncbot.organization.OrganizationRepo
import dev.kovstas.adsyncbot.telegram.TgChatId
import dev.kovstas.adsyncbot.user.User.UserId
import java.time.Instant
import java.util.UUID

trait UserService[F[_]] {
  def checkOrCreateNewUser(tgChatId: TgChatId, name: Option[String]): F[_]


  def findUserByChat(tgUserId: TgChatId): F[Option[User]]
  def findUserById(id: UserId): F[Option[User]]

  def getUserChat(id: UserId): F[PrivateChat]

  def createUser(
      name: Option[String],
      tgChatId: TgChatId
  ): F[UserId]

  def activateAndGetUserState(
      tgChatId: TgChatId
  ): F[Option[UserStateInfo]]
}

final class DefaultUserService(
    userRepo: UserRepo[IO],
    organizationRepo: OrganizationRepo[IO]
) extends UserService[IO] {
  override def findUserByChat(tgUserId: TgChatId): IO[Option[User]] =
    None.pure[IO]

  override def findUserById(id: UserId): IO[Option[User]] = Some(
    User(
      id,
      "test",
      TgChatId(83020144),
      isActive = true,
      Instant.now(),
      Instant.now()
    )
  ).pure[IO]

  override def createUser(
      name: Option[String],
      tgChatId: TgChatId
  ): IO[UserId] = IO(UserId(UUID.randomUUID()))

  override def activateAndGetUserState(
      tgChatId: TgChatId
  ): IO[Option[UserStateInfo]] =
    userRepo
      .userByChatId(tgChatId)
      .flatMap(
        _.traverse { user =>
          userRepo.activateUser(tgChatId) *>
            organizationRepo
              .getOrganizationByUserId(user.id)
              .map(UserStateInfo(user, _))
        }
      )

  override def getUserChat(id: UserId): IO[PrivateChat] = {
    userRepo
      .userById(id)
      .flatMap {
        case Some(user) if user.isActive => user.pure[IO]
        case Some(_)                     => IO.raiseError(UserInActive(id))
        case None                        => IO.raiseError(UserNotFound(id))
      }
      .map(u => PrivateChat(u.tgChatId.value, None, None, None))
  }

}
