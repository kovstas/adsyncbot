package dev.kovstas.adsyncbot.az.model

import io.circe.generic.JsonCodec

@JsonCodec(decodeOnly = true)
final case class UsersResponse(
    value: List[UserResponse]
)
