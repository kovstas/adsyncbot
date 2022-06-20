package dev.kovstas.burningbot.auth

import scala.util.control.NoStackTrace

final case class AuthenticationError(cause: String) extends NoStackTrace {
  override def getMessage: String = s"Authentication error: - '$cause'"
}
