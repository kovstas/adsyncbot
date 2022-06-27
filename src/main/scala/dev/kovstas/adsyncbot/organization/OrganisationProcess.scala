package dev.kovstas.adsyncbot.organization

import cats.effect.Temporal
import org.typelevel.log4cats.Logger
import scala.concurrent.duration.DurationInt

final class OrganizationProcess[F[_]: Temporal: Logger](
    organizationService: OrganizationService[F]
) {

  val process: fs2.Stream[F, Unit] =
    (fs2.Stream.emit(()) ++ fs2.Stream.fixedDelay[F](1.minutes))
      .evalTap(_ => Logger[F].debug("Starting OrganizationProcess"))
      .evalMap { _ =>
        organizationService.checkOrganizationState
      }

}
