package dev.kovstas.burningbot.teamMember

import cats.Applicative
import cats.implicits.{catsSyntaxApplicativeId, none}
import dev.kovstas.burningbot.model.TeamMember
import dev.kovstas.burningbot.model.TeamMember.TgUserId

trait TeamMemberRepo[F[_]] {
  def getUserByTgId(tgId: TgUserId): F[Option[TeamMember]]
}

final class PostgresTeamMemberRepo[F[_]: Applicative]
    extends TeamMemberRepo[F] {
  override def getUserByTgId(tgId: TgUserId): F[Option[TeamMember]] =
    none[TeamMember].pure[F]
}
