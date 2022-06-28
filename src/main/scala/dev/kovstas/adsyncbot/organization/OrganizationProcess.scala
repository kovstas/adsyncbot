package dev.kovstas.adsyncbot.organization

import cats.effect.Temporal
import org.typelevel.log4cats.StructuredLogger
import scala.concurrent.duration.DurationInt

final class OrganizationProcess[F[_]: Temporal: StructuredLogger](
    organizationService: OrganizationService[F]
) {

  val process: fs2.Stream[F, Unit] =
    (fs2.Stream.emit(()) ++ fs2.Stream.fixedDelay[F](1.minutes))
      .evalTap(_ => StructuredLogger[F].debug("Starting OrganizationProcess"))
      .evalMap { _ =>
        organizationService.checkOrganizationState
      }

}
