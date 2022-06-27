package dev.kovstas.adsyncbot.auth.model

import cats.effect.kernel.Concurrent
import cats.syntax.all._
import dev.kovstas.adsyncbot.user.User.UserId
import java.util.UUID
import org.http4s.{
  DecodeFailure,
  DecodeResult,
  EntityDecoder,
  InvalidMessageBodyFailure,
  UrlForm
}
import scala.util.Try

final case class OrganizationMemberLoginRequest(
    userId: UserId,
    accessToken: String
)

object OrganizationMemberLoginRequest {
  import UrlForm._

  implicit def decoder[F[_]: Concurrent]
      : EntityDecoder[F, OrganizationMemberLoginRequest] =
    EntityDecoder[F, UrlForm].flatMapR { form =>
      DecodeResult[F, OrganizationMemberLoginRequest](
        (
          form.values
            .get("state")
            .flatMap(v =>
              Try(UserId(UUID.fromString(v.toList.mkString))).toOption
            )
            .toValidNel("the state field isn't found or invalid either"),
          form.values
            .get("access_token")
            .map(_.toList.mkString)
            .toValidNel("the access_token field isn't found")
        ).mapN(OrganizationMemberLoginRequest.apply)
          .toEither
          .leftMap[DecodeFailure] { e =>
            InvalidMessageBodyFailure(e.toList.mkString(", "))
          }
          .pure[F]
      )

    }

}
