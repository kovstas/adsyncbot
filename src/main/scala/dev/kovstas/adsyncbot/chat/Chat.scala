package dev.kovstas.adsyncbot.chat

import dev.kovstas.adsyncbot.chat.Chat.ChatId
import dev.kovstas.adsyncbot.organization.Organization.OrganizationId
import dev.kovstas.adsyncbot.organization.OrganizationMember.OrganizationMemberId
import dev.kovstas.adsyncbot.telegram.TgChatId
import doobie.Meta
import doobie.postgres.implicits._
import io.estatico.newtype.macros.newtype
import java.time.Instant
import java.util.UUID

final case class Chat(
    id: ChatId,
    tgChatId: TgChatId,
    organizationId: OrganizationId,
    name: Option[String],
    createdAt: Instant,
    updatedAt: Instant
)

object Chat {
  @newtype case class ChatId(value: UUID)
  object ChatId {
    implicit val meta: Meta[ChatId] = deriving[Meta]
  }
}

final case class ChatMember(
    chatId: ChatId,
    organizationMemberId: OrganizationMemberId,
    role: ChatMemberRole,
    createdAt: Instant,
    updatedAt: Instant
)
