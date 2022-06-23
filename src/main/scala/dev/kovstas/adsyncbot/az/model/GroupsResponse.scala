package dev.kovstas.adsyncbot.az.model

import dev.kovstas.adsyncbot.group.Group.AdGroupId
import io.circe.generic.JsonCodec

@JsonCodec(decodeOnly = true)
final case class GroupsResponse(
    value: List[GroupsResponse]
)

object GroupsResponse {

  @JsonCodec(decodeOnly = true)
  final case class GroupResponse(
      id: AdGroupId,
      displayName: String
  )

}
