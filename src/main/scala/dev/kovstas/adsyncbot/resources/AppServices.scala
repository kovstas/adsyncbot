package dev.kovstas.adsyncbot.resources

import canoe.api.TelegramClient
import cats.effect.kernel.Concurrent
import dev.kovstas.adsyncbot.auth.{
  DefaultOAuthClient,
  DefaultOAuthService,
  OAuthService
}
import dev.kovstas.adsyncbot.az.{
  DefaultApplicationGraphApi,
  DefaultUserGraphApi
}
import dev.kovstas.adsyncbot.chat.{ChatService, DefaultChatService}
import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.organization.{
  DefaultOrganizationService,
  OrganizationService
}
import dev.kovstas.adsyncbot.user.{DefaultUserService, UserService}
import org.http4s.client.Client
import org.typelevel.log4cats.StructuredLogger

final class AppServices[F[_]](
    val organizationService: OrganizationService[F],
    val userService: UserService[F],
    val chatService: ChatService[F],
    val oAuthService: OAuthService[F]
)

object AppServices {

  def apply[F[_]: Concurrent: TelegramClient: StructuredLogger](
      repos: Repos[F],
      httpClient: Client[F],
      config: MsConfig
  ): AppServices[F] = {

    val authClient = new DefaultOAuthClient[F](
      httpClient,
      config
    )

    val appGraphApi =
      new DefaultApplicationGraphApi[F](authClient, httpClient, config)

    val userGraphApi =
      new DefaultUserGraphApi[F](httpClient, config)

    val chatService = new DefaultChatService[F](
      repos.organizationRepo,
      repos.userRepo,
      repos.chatRepo
    )

    val organizationService = new DefaultOrganizationService[F](
      appGraphApi,
      userGraphApi,
      repos.organizationRepo,
      chatService
    )

    val userService = new DefaultUserService[F](
      repos.userRepo,
      repos.organizationRepo,
      config
    )

    val oAuthService = new DefaultOAuthService[F](
      organizationService,
      chatService,
      config
    )

    new AppServices(
      organizationService,
      userService,
      chatService,
      oAuthService
    )

  }

}
