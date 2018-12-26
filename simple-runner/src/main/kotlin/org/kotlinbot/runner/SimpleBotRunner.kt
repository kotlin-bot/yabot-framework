package org.kotlinbot.runner

import name.alatushkin.api.vk.MethodExecutorImpl
import name.alatushkin.api.vk.throwExceptionsOnError
import name.alatushkin.api.vk.withToken
import name.alatushkin.httpclient.httpClient
import org.kotlinbot.api.*
import org.kotlinbot.api.inevents.Origin
import org.kotlinbot.api.methods.AttachmentResolver
import org.kotlinbot.api.methods.AttachmentResolverRouter
import org.kotlinbot.connectors.tg.TgAttachmentResolver
import org.kotlinbot.connectors.tg.TgLongPoller
import org.kotlinbot.connectors.tg.TgProfileResolver
import org.kotlinbot.connectors.tg.TgReplyService
import org.kotlinbot.connectors.vk.VkAttachmentResolver
import org.kotlinbot.connectors.vk.VkProfileResolver
import org.kotlinbot.connectors.vk.VkReplyService
import org.kotlinbot.connectors.vk.longpoll.VkLongPoller
import org.kotlinbot.core.BotRunner
import org.kotlinbot.core.BotShell
import org.kotlinbot.core.BotStateRepository

import kotlin.concurrent.thread

class SimpleBotRunner(
    val botShell: BotShell,
    val stateRepository: BotStateRepository = BotStateRepositoryInMemoryImpl()
) {
    private val profileResolvers = HashMap<Origin, ProfileResolver>()
    private val replyServices = HashMap<Origin, ReplyService>()
    private val attachmentResolvers = HashMap<Origin, AttachmentResolver>()
    private val httpClient = httpClient()
    private val runner: BotRunner

    init {
        runner = BotRunner(
            botShell = botShell,
            botStateRepository = stateRepository,
            profileResolver = ProfileResolverRouter(
                profileResolvers
            )
        )
    }

    private fun updateReplyServices() {
        runner.regitsterService(ReplyService::class.java, RouteReplyService(replyServices).retryOnIoErrors(5))
    }

    fun connectVk(groupId: Long, token: String): SimpleBotRunner {
        ensureClassExists(
            "org.kotlinbot.connectors.vk.longpoll.VkLongPoller",
            "to connect bot to VK add vk-connector dependency"
        )
        val vkApi = MethodExecutorImpl(httpClient).withToken(token).throwExceptionsOnError()
        profileResolvers[Origin.VK] = VkProfileResolver(vkApi)
        attachmentResolvers[Origin.VK] = VkAttachmentResolver(vkApi)
        updateAttachmentResolvers()

        val vkReplyService = VkReplyService(groupId.toString(), vkApi)
        replyServices[Origin.VK] = vkReplyService
        updateReplyServices()

        val vkPoller = VkLongPoller(
            token = token,
            groupId = groupId,
            eventConsumer = runner.handleExceptions()
        )

        thread {
            vkPoller.doJob()
        }

        return this
    }

    fun connectTg(token: String): SimpleBotRunner {
        ensureClassExists(
            "org.kotlinbot.connectors.tg.TgLongPoller",
            "to connect bot to TG add tg-connector dependency"
        )
        profileResolvers[Origin.TG] = TgProfileResolver()
        attachmentResolvers[Origin.TG] = TgAttachmentResolver(token)
        updateAttachmentResolvers()

        val tgReplyService = TgReplyService(token, httpClient)
        replyServices[Origin.TG] = tgReplyService
        updateReplyServices()

        val tgPoller = TgLongPoller(
            token = token,
            eventConsumer = runner.handleExceptions()
        )

        thread {
            tgPoller.doJob()
        }

        return this
    }

    private fun updateAttachmentResolvers() {
        runner.regitsterService(
            AttachmentResolver::class.java,
            AttachmentResolverRouter(attachmentResolvers)
        )
    }
}