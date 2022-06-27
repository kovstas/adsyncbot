package dev.kovstas.adsyncbot.resources

import cats.effect.MonadCancelThrow
import dev.kovstas.adsyncbot.chat.{ChatRepo, PostgresChatRepo}
import dev.kovstas.adsyncbot.organization.{
  OrganizationRepo,
  PostgresOrganizationRepo
}
import dev.kovstas.adsyncbot.user.{PostgresUserRepo, UserRepo}
import doobie.util.transactor.Transactor
import java.time.{Clock, ZoneId}

class Repos[F[_]](
    val userRepo: UserRepo[F],
    val organizationRepo: OrganizationRepo[F],
    val chatRepo: ChatRepo[F]
)

object Repos {

  def apply[F[_]: MonadCancelThrow](transactor: Transactor[F]): Repos[F] = {
    val clock = Clock.system(ZoneId.systemDefault())
    new Repos(
      new PostgresUserRepo(clock, transactor),
      new PostgresOrganizationRepo(clock, transactor),
      new PostgresChatRepo(clock, transactor)
    )

  }

}
