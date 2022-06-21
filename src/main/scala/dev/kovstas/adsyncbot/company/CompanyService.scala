package dev.kovstas.adsyncbot.company

import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import dev.kovstas.adsyncbot.model.Company.ADTenantId
import dev.kovstas.adsyncbot.model.{Company, CompanyUser}
import dev.kovstas.adsyncbot.user.User.UserId
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait CompanyService[F[_]] {
  def findCompanyByUserId(userId: UserId): F[Option[Company]]
  def findCompanyUserByUserId(userId: UserId): F[List[CompanyUser]]
  def createCompany(
      userId: UserId,
      tenantId: ADTenantId,
      accessToken: String
  ): F[Unit] // should create company user
}

object MockCompanyService extends CompanyService[IO] {
  private val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def findCompanyByUserId(userId: UserId): IO[Option[Company]] =
    None.pure[IO]

  override def findCompanyUserByUserId(userId: UserId): IO[List[CompanyUser]] =
    Nil.pure[IO]

  override def createCompany(
      userId: UserId,
      tenantId: ADTenantId,
      accessToken: String
  ): IO[Unit] =
    logger.info("Company created")

}
