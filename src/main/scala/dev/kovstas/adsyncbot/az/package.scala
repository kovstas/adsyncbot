package dev.kovstas.adsyncbot

package object az {

  val UserGraphParams: Map[String, String] = Map(
    "$select" -> "displayName,businessPhones,id,mail,accountEnabled"
  )

}
