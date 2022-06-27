package dev.kovstas.adsyncbot.chat

import doobie.Meta
import enumeratum.EnumEntry.Lowercase
import enumeratum._
import doobie.postgres.implicits.pgEnumString

sealed trait ChatMemberRole extends Lowercase

object ChatMemberRole extends Enum[ChatMemberRole] {
  case object Owner extends ChatMemberRole
  case object Admin extends ChatMemberRole
  case object Member extends ChatMemberRole

  override def values: IndexedSeq[ChatMemberRole] = findValues

  implicit val meta: Meta[ChatMemberRole] = pgEnumString[ChatMemberRole](
    "chat_member_role",
    ChatMemberRole.withName,
    _.entryName
  )
}
