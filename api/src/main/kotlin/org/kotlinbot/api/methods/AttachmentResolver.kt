package org.kotlinbot.api.methods

import org.kotlinbot.api.inevents.*

interface AttachmentResolver {
    suspend fun resolve(attacheReference: PhotoReference): PhotoAttachment
    suspend fun resolve(attacheReference: LocationReference): LocationAttachment
    suspend fun resolve(attacheReference: FileReference): FileAttachment
    suspend fun resolve(attacheReference: VoiceReference): VoiceAttachment
}

class AttachmentResolverRouter(val resolvers:Map<Origin,AttachmentResolver>):AttachmentResolver{
    override suspend fun resolve(attacheReference: PhotoReference): PhotoAttachment {
        return resolvers[attacheReference.origin]!!.resolve(attacheReference)
    }

    override suspend fun resolve(attacheReference: LocationReference): LocationAttachment {
        return resolvers[attacheReference.origin]!!.resolve(attacheReference)
    }

    override suspend fun resolve(attacheReference: FileReference): FileAttachment {
        return resolvers[attacheReference.origin]!!.resolve(attacheReference)
    }

    override suspend fun resolve(attacheReference: VoiceReference): VoiceAttachment {
        return resolvers[attacheReference.origin]!!.resolve(attacheReference)
    }

}