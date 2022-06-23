package dev.kovstas.adsyncbot.az

import dev.kovstas.adsyncbot.az.model.{OrganizationResponse, UserResponse}

trait UserGraphApi[F[_]] {
  def organisation(accessToken: String): F[OrganizationResponse]
  def me(accessToken: String): F[UserResponse]
}
