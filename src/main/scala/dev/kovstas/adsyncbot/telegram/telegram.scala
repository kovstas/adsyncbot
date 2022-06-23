package dev.kovstas.adsyncbot

import canoe.models.InlineKeyboardButton
import dev.kovstas.adsyncbot.auth.OAuthHelper.companyMemberAuthoriseLink
import dev.kovstas.adsyncbot.config.MsAuthConfig
import dev.kovstas.adsyncbot.user.User.UserId
import io.estatico.newtype.macros.newtype

package object telegram {
  @newtype case class TgChatId(value: Long)

  def loginAsOrganizationMemberButton(
      config: MsAuthConfig,
      userId: UserId
  ): InlineKeyboardButton =
    InlineKeyboardButton.url(
      "Connect as a company member",
      companyMemberAuthoriseLink(
        config,
        userId
      ).renderString
    )
}
