package dev.kovstas.adsyncbot

import io.estatico.newtype.macros.newtype

package object model {
  @newtype case class TgChatId(value: Long)
}
