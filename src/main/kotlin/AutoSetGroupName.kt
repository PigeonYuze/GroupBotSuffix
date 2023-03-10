package com.pigeonyuze

import com.pigeonyuze.Config.SetNameType.*
import com.pigeonyuze.execute.RandomText
import com.pigeonyuze.execute.SystemData
import com.pigeonyuze.execute.SystemData.toPercentage
import com.pigeonyuze.execute.TimeData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.utils.info
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.fixedRateTimer

object GroupBotSuffix : KotlinPlugin(
    JvmPluginDescription(
        id = "com.pigeonyuze.group-bot-suffix",
        name = "GroupBotSuffix",
        version = "1.2.1",
    ) {
        author("鸽子宇泽")
        info("""自动设置bot在所有群聊的群名片信息，可用设置为倒计时以及相关内容""")
    }
) {
    private val botsList = mutableListOf<Bot>()

    override fun onEnable() {
        Config.reload()
        if (Config.open == HOW_LONG_TO_DISTANCE) {
            logger.warning("配置中存在旧的`open`设置, 使用了过时的'HOW_LONG_TO_DISTANCE'!  请及时修改！")
        }
        /* Mirai-Console 内的生命周期 */
        val parentScope = GlobalEventChannel.parentScope(this)
        parentScope.subscribeAlways<BotOnlineEvent> {
            if (botsList.contains(this.bot)) return@subscribeAlways
            botsList.add(bot)
        }
        parentScope.subscribeAlways<BotOfflineEvent> {
            if (botsList.contains(this.bot)) botsList.remove(bot)
        }
        logger.info { "Try to start task" }
        fixedRateTimer(
            name = "SetNameTask",
            period = Config.waitTimeMS,
            initialDelay = Config.waitTimeMS
        ) {
            launch {
                val newSuffix = Config.separator + parseContent()
                if (Config.allowlist.isNotEmpty() && Config.allowlist[0] != 114514L) {
                    for (groupId in Config.allowlist) {
                        for (bot in botsList) {
                            var group: Group?
                            if (bot.getGroup(groupId).also { group = it } == null) continue
                            group!!.botAsMember.nameCard = bot.nick + newSuffix
                        }
                    }
                    return@launch
                }
                for (bot in botsList) {
                    for (group in bot.groups) {
                        group.botAsMember.nameCard = bot.nick + newSuffix
                        delay(Config.waitGroupMS)
                    }
                }
            }.start()
            logger.debug("Set over.")
        }
        logger.info { "Start task!" }
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
            HOW_LONG_TO_DISTANCE -> {
                TimeData.getHowLongToDistanceImpl(arg ?: throwSuppressArgs())
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