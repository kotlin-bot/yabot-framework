package org.kotlinbot.api.inevents


interface PhotoReference {
    val origin: Origin
    val id: String
    val width: Int?
    val height: Int?
}

data class PhotoReferenceItem(
    override val origin: Origin, override val id: String,
    override val width: Int? = null,
    override val height: Int? = null
) : PhotoReference

data class PhotoAttachment(
    override val origin: Origin,
    override val id: String,
    override val width: Int,
    override val height: Int,
    val fileUrl: String
) : PhotoReference


interface LocationReference{
    val origin: Origin
}
data class LocationAttachment(
    override val origin: Origin,
    val location: LatLng,
    val place: Place? = null
) : LocationReference


interface FileReference {
    val origin: Origin
    val id: String
}

data class FileReferenceItem(override val origin: Origin, override val id: String) : FileReference
data class FileAttachment(
    val title: String,
    val size: Int,
    val url: String,
    val extension: String,
    override val origin: Origin,
    override val id: String
) : FileReference

interface VoiceReference {
    val origin: Origin
    val id: String
}

data class VoiceReferenceItem(override val origin: Origin, override val id: String) : VoiceReference
data class VoiceAttachment(
    val duration: Int,
    val url: String,
    override val origin: Origin,
    override val id: String
) : VoiceReference


data class LatLng(
    val lat: Double,
    val lng: Double
)

data class Place(
    val title: String,
    val address: String
)