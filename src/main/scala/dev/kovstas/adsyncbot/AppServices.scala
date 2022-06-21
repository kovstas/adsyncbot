package dev.kovstas.adsyncbot

import cats.effect.IO
import dev.kovstas.adsyncbot.company.{CompanyService, MockCompanyService}
import dev.kovstas.adsyncbot.user.{MockUserService, UserService}

final case class AppServices(
    companyService: CompanyService[IO] = MockCompanyService,
    userService: UserService[IO] = MockUserService
)
