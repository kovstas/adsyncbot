package dev.kovstas.adsyncbot

import canoe.models.InlineKeyboardButton
import dev.kovstas.adsyncbot.auth.OAuthHelper.organizationMemberAuthoriseLink
import dev.kovstas.adsyncbot.config.MsConfig
import dev.kovstas.adsyncbot.user.User.UserId
import doobie.Meta
import io.estatico.newtype.macros.newtype

package object telegram {
  @newtype case class TgChatId(value: Long)

  object TgChatId {
    implicit val meta: Meta[TgChatId] = deriving[Meta]
  }

  def loginAsOrganizationMemberButton(
      config: MsConfig,
      userId: UserId
  ): InlineKeyboardButton =
    InlineKeyboardButton.url(
      "Connect as a organization member",
      organizationMemberAuthoriseLink(
        config,
        userId
      ).renderString
    )
}
