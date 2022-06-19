package dev.kovstas.burningbot.model

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed trait TeamMemberRole extends EnumEntry with Snakecase

object TeamMemberRole extends Enum[TeamMemberRole] {
  case object Lead extends TeamMemberRole
  case object Member extends TeamMemberRole

  override def values: IndexedSeq[TeamMemberRole] = findValues
}
