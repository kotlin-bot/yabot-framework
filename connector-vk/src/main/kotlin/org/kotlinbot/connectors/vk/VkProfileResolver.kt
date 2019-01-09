package org.kotlinbot.connectors.vk

import name.alatushkin.api.vk.MethodExecutorWithException
import name.alatushkin.api.vk.generated.common.Sex
import name.alatushkin.api.vk.generated.users.methods.UsersGetMethod
import org.kotlinbot.api.PersonProfile
import org.kotlinbot.api.ProfileResolver
import org.kotlinbot.api.ProfileValue
import org.kotlinbot.api.Source
import org.kotlinbot.api.inevents.InEvent
import org.kotlinbot.connectors.vk.longpoll.VkUserId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.MonthDay

class VkProfileResolver(val api: MethodExecutorWithException) : ProfileResolver {
    override suspend fun resolveProfileFromEvent(event: InEvent): PersonProfile? {
        if (!event.origin.isVkontakte)
            return null

        val userIdStr = (event.personId as VkUserId).id.toString()
        try {

            val result = api(
                UsersGetMethod(
                    userIds = arrayOf(userIdStr),
                    fields = arrayOf("bdate", "sex", "age")
                )
            ).first()

            val value = result.sex
            return PersonProfile(
                personId = event.personId,
                birthDayValue = result.bdate?.let { fromProfile(MonthDay.of(it.month, it.day)) },
                birthYearValue = result.bdate?.year?.let { fromProfile(it) },
                firstNameValue = fromProfile(result.firstName),
                lastNameValue = fromProfile(result.lastName),
                sexValue = result.sex?.let { fromProfile(translateSex(it)) },
                resolved = true

            )
        } catch (e: Exception) {
            logger.warn("Cant resolve VK profile {} cause intentHandlerOf", userIdStr, e)
            return null
        }
    }

    private fun <T> fromProfile(value: T): ProfileValue<T> {
        return ProfileValue(value, Source.PROFILE)
    }

    private fun translateSex(value: Sex): org.kotlinbot.api.Sex {
        return when (value) {
            Sex.FEMALE -> org.kotlinbot.api.Sex.FEMALE
            Sex.MALE -> org.kotlinbot.api.Sex.MALE
            Sex.UNKNOWN -> org.kotlinbot.api.Sex.UNKNOWN
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(VkProfileResolver::class.java)
    }

}