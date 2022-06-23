package dev.kovstas.adsyncbot

import cats.effect.IO
import dev.kovstas.adsyncbot.organization.{OrganizationService, MockCompanyService}
import dev.kovstas.adsyncbot.user.{MockUserService, UserService}

final case class AppServices(
                              companyService: OrganizationService[IO] = MockCompanyService,
                              userService: UserService[IO] = MockUserService
)
