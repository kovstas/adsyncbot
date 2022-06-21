package dev.kovstas.adsyncbot.auth

import dev.kovstas.adsyncbot.user.User.UserId
import scala.util.control.NoStackTrace

final case class AuthenticationError(cause: String) extends NoStackTrace {
  override def getMessage: String = s"Authentication error: - '$cause'"
}

final case class UserNotFound(userId: UserId) extends NoStackTrace {
  override def getMessage: String = s"The user with '$userId' isn't found"
}
