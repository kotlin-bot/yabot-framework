package org.kotlinbot.api

import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.api.inevents.Origin

interface ProfileResolver {
    suspend fun resolveProfileFromEvent(event: InEvent): UserProfile?
}


class ProfileResolverRouter(val profileResolvers: Map<Origin, ProfileResolver>) :
    ProfileResolver {
    override suspend fun resolveProfileFromEvent(event: InEvent): UserProfile? {
        return profileResolvers[event.origin]?.resolveProfileFromEvent(event)
    }
}

