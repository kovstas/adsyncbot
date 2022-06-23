package dev.kovstas.adsyncbot.user

import dev.kovstas.adsyncbot.organization.Organization

final case class UserStateInfo(
    user: User,
    organization: Option[Organization]
)
