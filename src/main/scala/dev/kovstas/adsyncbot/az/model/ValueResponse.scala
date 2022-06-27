package dev.kovstas.adsyncbot.az.model

import cats.effect.kernel.Concurrent
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class ValueResponse[T](
    value: T
)

object ValueResponse {
  implicit def decoder[T: Decoder]: Decoder[ValueResponse[T]] =
    Decoder.instance { c =>
      for {
        value <- c.downField("value").downArray.as[T]
      } yield ValueResponse(value)
    }

  implicit def http4sDecoder[F[_]: Concurrent, T: Decoder]
      : EntityDecoder[F, ValueResponse[T]] =
    jsonOf[F, ValueResponse[T]]
}
