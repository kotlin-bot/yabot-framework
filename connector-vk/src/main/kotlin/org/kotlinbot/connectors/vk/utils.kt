package org.kotlinbot.connectors.vk

import kotlinx.coroutines.delay
import name.alatushkin.api.vk.MethodExecutor
import name.alatushkin.api.vk.VkMethod
import name.alatushkin.api.vk.api.VkResponse
import name.alatushkin.api.vk.api.toAttachmentId
import name.alatushkin.api.vk.generated.docs.Doc
import name.alatushkin.api.vk.generated.photos.Photo
import name.alatushkin.httpclient.HttpClient
import org.kotlinbot.api.inevents.FileAttachment
import org.kotlinbot.api.inevents.Origin
import org.kotlinbot.api.inevents.PhotoAttachment

fun vkPhotoToAttachment(photo: Photo): PhotoAttachment {
    val largestPhoto = photo.sizes?.sortedBy { it.width }!!.last()
    return PhotoAttachment(
        id = photo.toAttachmentId(),
        origin = Origin.VK,
        width = largestPhoto.width!!.toInt(),
        height = largestPhoto.height!!.toInt(),
        fileUrl = largestPhoto.url!!
    )
}

fun vkDocToAttachment(doc: Doc): FileAttachment {

    return FileAttachment(
        id = doc.toAttachmentId(),
        origin = Origin.VK,
        title = doc.title,
        size = doc.size.toInt(),
        url = doc.url!!,
        extension = doc.ext
    )
}

fun MethodExecutor.threshold(): MethodExecutor {
    return object : MethodExecutor {
        override suspend fun <T> invoke(method: VkMethod<T>): VkResponse<T> {
            delay(300)
            return this@threshold(method)
        }

        override val httpClient: HttpClient
            get() = this@threshold.httpClient
    }
}