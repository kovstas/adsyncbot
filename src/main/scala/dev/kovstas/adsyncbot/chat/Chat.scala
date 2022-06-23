package dev.kovstas.adsyncbot.chat

import dev.kovstas.adsyncbot.chat.Chat.ChatId
import dev.kovstas.adsyncbot.organization.Organization.OrganizationId
import dev.kovstas.adsyncbot.organization.OrganizationMember.OrganizationMemberId
import dev.kovstas.adsyncbot.telegram.TgChatId
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class Chat(
    id: ChatId,
    tgChatId: TgChatId,
    organizationId: Option[OrganizationId],
    name: String,
    createdAt: Instant,
    updatedAt: Instant
)

object Chat {
  @newtype case class ChatId(value: UUID)
}

final case class ChatUser(
    chatId: ChatId,
    organizationMemberId: OrganizationMemberId,
    isAdmin: Boolean,
    isOwner: Boolean
)
