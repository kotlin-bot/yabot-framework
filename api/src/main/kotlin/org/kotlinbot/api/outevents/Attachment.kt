package org.kotlinbot.api.outevents

import name.alatushkin.httpclient.HttpClient
import name.alatushkin.httpclient.HttpMethod
import java.nio.file.Path


interface Attachment:PhotoAttachment,FileAttachment{
    val fileName: String
}

data class LocalFileAttachment(val filePath: Path) : Attachment{
    override val fileName: String
        get() = filePath.fileName.toString()
}

data class UrlFileAttachment(override val fileName: String, val url: String) : Attachment
data class BytesAttachment(override val fileName: String, val bytes: ByteArray) : Attachment

suspend fun Attachment.toBytesAttachment(httpClient: HttpClient): BytesAttachment {
    return when (this) {
        is BytesAttachment -> this
        is LocalFileAttachment -> BytesAttachment(this.filePath.fileName.toString(), filePath.toFile().readBytes())
        is UrlFileAttachment -> BytesAttachment(this.fileName, httpClient(HttpMethod.GET(this.url)).data)
        else -> error("unknonwn attachment type")
    }
}

interface PhotoAttachment {
    companion object {
        fun ofLocalFile(filePath: Path): PhotoAttachment {
            return LocalFileAttachment(filePath)
        }

        fun fromUrl(url: String): PhotoAttachment {
            return UrlFileAttachment("image.jpg", url)
        }

        fun fromByteArray(bytes: ByteArray): PhotoAttachment {
            return BytesAttachment("image.jpg", bytes)
        }
    }
}

interface FileAttachment {
    companion object {
        fun ofLocalFile(filePath: Path): FileAttachment {
            return LocalFileAttachment(filePath)
        }

        fun fromUrl(fileName: String, url: String): FileAttachment {
            return UrlFileAttachment(fileName, url)
        }

        fun fromByteArray(fileName: String, bytes: ByteArray): FileAttachment {
            return BytesAttachment(fileName, bytes)
        }
    }
}

