package com.pigeonyuze

import com.pigeonyuze.Config.SetNameType.*
import com.pigeonyuze.command.SetNamePermissionCommand
import com.pigeonyuze.execute.RandomText
import com.pigeonyuze.execute.SystemData
import com.pigeonyuze.execute.SystemData.toPercentage
import com.pigeonyuze.execute.TimeData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.warning
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GroupBotSuffix : KotlinPlugin(
    JvmPluginDescription(
        id = "com.pigeonyuze.group-bot-suffix",
        name = "GroupBotSuffix",
        version = "1.3.0",
    ) {
        author("鸽子宇泽")
        info("""自动设置bot在所有群聊的群名片信息，可用设置为倒计时以及相关内容""")
    }
) {

    private val botsList = mutableListOf<Bot>()

    /**
     * 惰性的修改方式，仅会在发送信息前更改
     *
     * 仅当为惰性修改模式时，此值才会被初始化
     * */
    private lateinit var toChangeNames: MutableMap<Long, String>

    /**
     * 上一次修改的时间
     * - Key 为 群号
     * - Value 为修改的毫秒时间戳
     *
     * 仅当为惰性修改模式时，此值才会被初始化
     * */
    private lateinit var lastedChangeTime: MutableMap<Long, Long>

    // 理论上的最大值
    private const val MAX_CALL_COUNT_EACH_SECOND = 30

    private var called = 0
        set(value) {
            if (value == MAX_CALL_COUNT_EACH_SECOND + 1) {
                logger.warning { "超过了每分钟最大修改次数 $MAX_CALL_COUNT_EACH_SECOND! 接下来的请求将会自动拦截" }
            }
            if (value == 0 && field > MAX_CALL_COUNT_EACH_SECOND) {
                logger.info("已重制计时器, 接下来的请求会自动处理")
            }
            field = value
        }

    val allowlist by lazy {
        PluginPermission(Config.allowlist)
    }


    override fun onEnable() {
        Config.reload()

        allowlist // Load value

        CommandManager.registerCommand(SetNamePermissionCommand)

        /* Mirai-Console 内的生命周期 */
        val parentScope = GlobalEventChannel.parentScope(this)
        if (Config.isModeLazy) {
            toChangeNames = LinkedHashMap()
            lastedChangeTime = LinkedHashMap()
            parentScope.subscribeAlways<GroupMessagePreSendEvent> {
                val lastedTime = lastedChangeTime[this.target.id]
                if (lastedTime != null && System.nanoTime() - lastedTime < Config.waitTimeMS) {
                    /* 跳过 未到时间. */
                    return@subscribeAlways
                }
                if (Config.allowlist.isNotEmpty() && !allowlist.hasPermission(this.target.id)) {
                    /* 跳过 不在白名单中. */
                    return@subscribeAlways
                }
                val newName = toChangeNames[this.target.id]
                    ?: throw RuntimeException("出现了意向不到的错误: 找不到新的名称. 请将以下数据反馈到 git 仓库\n将要修改的群目标: ${target.id} 运行时数据: $lastedChangeTime , $toChangeNames")
                target.botAsMember.nameCard = newName
            }
        } else {
            logger.warning { "您正在使用非惰性的修改群名方式，这可能会导致一些可能的风险" }
            logger.warning { "详细可见链接: https://github.com/PigeonYuze/GroupBotSuffix/issues/11" }
        }

        parentScope.subscribeAlways<BotOnlineEvent> {
            if (botsList.contains(this.bot)) return@subscribeAlways
            botsList.add(bot)
        }
        parentScope.subscribeAlways<BotOfflineEvent> {
            if (botsList.contains(this.bot)) botsList.remove(bot)
        }

        logger.info { "Try to start task" }

        // Handle changing name
        launch(context = this.coroutineContext) {
            delay(Config.waitTimeRange[Config.WAIT_TIME_RANGE_INDEX].first)
            while (true) {
                if (!Config.isModeLazy) {
                    logger.info { "Start change the name of bots in each groups!" }
                }

                implChangeName(
                    if (Config.isModeLazy) { name ->
                        toChangeNames[this.id] = name
                    } else { name ->
                        called++
                        if (called <= MAX_CALL_COUNT_EACH_SECOND) {
                            this.botAsMember.nameCard = name
                        }
                    }
                )

                logger.debug("Set over.")
                delay(Config.waitTimeRange[Config.WAIT_TIME_RANGE_INDEX].random())
            }
        }
        // Handle setting `called` to 0 every 60 seconds
        launch(context = this.coroutineContext) {
            delay(Config.waitTimeRange[Config.WAIT_TIME_RANGE_INDEX].first)
            while (true) {
                called = 0
                delay(60_000)
            }
        }


    }

    private suspend inline fun implChangeName(impl: Group.(String) -> Unit) {
        val newSuffix = Config.separator + parseContent()
        if (!allowlist.isAllowAll) {
            for (groupId in allowlist.white) {
                for (bot in botsList) {
                    var group: Group?
                    if (bot.getGroup(groupId).also { group = it } == null) continue
                    delay(150L)
                    impl(group!!, bot.nick + newSuffix)
                }
                delay(Config.waitTimeRange[Config.WAIT_GROUP_RANGE_INDEX].random())
            }
            return
        }
        for (bot in botsList) {
            for (group in bot.groups) {
                if (!allowlist.hasPermission(group.id)) continue
                impl(group, bot.nick + newSuffix)
                delay(Config.waitTimeRange[Config.WAIT_GROUP_RANGE_INDEX].random())
            }
        }
    }

    private fun parseContent(): String {
        var waitForVar = false
        var ret = Config.content
        var arg = StringBuilder()
        for (char in Config.content) {
            if (char == '%') {
                if (!waitForVar) {
                    waitForVar = true
                    continue
                }
                waitForVar = false
                ret = ret.replaceFirst("%$arg%", parseContentImpl(Config.open, arg.toString()))
                arg = arg.clear()
                continue
            }
            if (!waitForVar) continue
            if (char == 's') {
                waitForVar = false
                ret = ret.replaceFirst("%s", parseContentImpl(Config.open, null))
                arg = arg.clear()
                continue
            }
            arg.append(char)
        }
        if (arg.isNotEmpty()) {
            ret = ret.replaceFirst("%$arg", parseContentImpl(Config.open, arg.toString()))
        }
        logger.verbose("get-suffix: $ret")
        return ret
    }

    private fun parseContentImpl(type: Config.SetNameType, arg: String?): String {
        return when (type) {
            NOW_TIME -> {
                DateTimeFormatter.ofPattern(arg ?: "HH:mm:ss").format(LocalDateTime.now())
            }

            CPU_LOAD -> {
                SystemData.systemCpuLoad.toPercentage()
            }

            JVM_CPU_LOAD -> {
                SystemData.jvmCpuLoad.toPercentage()
            }

            MEMORY_LOAD -> {
                SystemData.systemMemory.toPercentage()
            }

            JVM_MEMORY_LOAD -> {
                SystemData.jvmMemory.toPercentage()
            }

            HOW_LONG_TO_DISTANCE_DETAIL -> {
                TimeData.getHowLongToDistanceDetailImpl(arg ?: throwSuppressArgs())
            }

            HOW_LONG_TO_DISTANCE_SIMPLE -> {
                TimeData.getHowLongToDistanceSimpleImpl(arg ?: throwSuppressArgs())
            }

            RANDOM_TEXT -> {
                RandomText.randomTextImpl(arg ?: throwSuppressArgs())
            }
        }
    }

    private fun throwSuppressArgs(): Nothing =
        throw IllegalArgumentException("Does not provide parameters (parameter is 's')")
}