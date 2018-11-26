package org.kotlinbot.api

import com.fasterxml.jackson.annotation.JsonIgnore
import org.kotlinbot.api.inevents.UserId
import java.time.LocalDateTime
import java.time.MonthDay

enum class Sex {
    UNKNOWN, MALE, FEMALE
}

enum class Source {
    NO, PROFILE, GUESSED, USER
}

enum class CanSendMessage {
    UNKNOWN, NO, YES
}

data class ProfileValue<T>(val value: T, val source: Source)

data class UserProfile(
    val userId: UserId,
    val created: LocalDateTime = LocalDateTime.now(),
    val canSendMessage: CanSendMessage = CanSendMessage.UNKNOWN,
    val resolved: Boolean = false,
    private val firstNameValue: ProfileValue<String>? = null,
    private val lastNameValue: ProfileValue<String>? = null,
    private val sexValue: ProfileValue<Sex>? = null,
    private val birthDayValue: ProfileValue<MonthDay>? = null,
    private val birthYearValue: ProfileValue<Int>? = null,
    private val languageValue: ProfileValue<String>? = null
) {
    @get:JsonIgnore
    val firstName: String?
        get() = firstNameValue?.value
    @get:JsonIgnore
    val lastName: String?
        get() = lastNameValue?.value
    @get:JsonIgnore
    val sex: Sex
        get() = sexValue?.value ?: Sex.UNKNOWN
    @get:JsonIgnore
    val birthDay: MonthDay?
        get() = birthDayValue?.value
    @get:JsonIgnore
    val birthYear: Int?
        get() = birthYearValue?.value

    @get:JsonIgnore
    val language: String?
        get() = languageValue?.value


    @get:JsonIgnore
    val firstNameSource: Source
        get() = firstNameValue?.source ?: Source.NO

    @get:JsonIgnore
    val lastNameSource: Source
        get() = lastNameValue?.source ?: Source.NO

    @get:JsonIgnore
    val sexSource: Source
        get() = sexValue?.source ?: Source.NO

    @get:JsonIgnore
    val birthDaySource: Source
        get() = birthDayValue?.source ?: Source.NO

    @get:JsonIgnore
    val birthYearSource: Source
        get() = birthYearValue?.source ?: Source.NO

    @get:JsonIgnore
    val languageSource: Source?
        get() = languageValue?.source ?: Source.NO

}