package dev.kovstas.adsyncbot.resources

import cats.effect.Resource
import cats.effect.kernel.Async
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import scala.concurrent.duration.DurationInt

object HttpClient {

  def make[F[_]: Async]: Resource[F, Client[F]] =
    EmberClientBuilder
      .default[F]
      .withIdleConnectionTime(1.minute)
      .withTimeout(3.minutes)
      .build

}
