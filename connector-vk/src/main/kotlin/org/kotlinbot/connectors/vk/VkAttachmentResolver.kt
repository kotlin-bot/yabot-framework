package org.kotlinbot.connectors.vk

import name.alatushkin.api.vk.MethodExecutorWithException
import name.alatushkin.api.vk.generated.photos.methods.PhotosGetByIdMethod
import org.kotlinbot.api.inevents.*
import org.kotlinbot.api.methods.AttachmentResolver

class VkAttachmentResolver(
    val api: MethodExecutorWithException
) : AttachmentResolver {
    override suspend fun resolve(attacheReference: PhotoReference): PhotoAttachment {
        if (attacheReference is PhotoAttachment)
            return attacheReference
        else {
            return vkPhotoToAttachment(
                api(
                    PhotosGetByIdMethod(
                        photos = arrayOf(attacheReference.id),
                        photoSizes = true
                    )
                ).first()
            )
        }
    }

    override suspend fun resolve(attacheReference: LocationReference): LocationAttachment {
        return attacheReference as LocationAttachment
    }

    override suspend fun resolve(attacheReference: FileReference): FileAttachment {
        return attacheReference as FileAttachment
    }

    override suspend fun resolve(attacheReference: VoiceReference): VoiceAttachment {
        return attacheReference as VoiceAttachment
    }

}