package com.pigeonyuze

import com.pigeonyuze.Config.SetNameType.*
import com.pigeonyuze.execute.SystemData
import com.pigeonyuze.execute.SystemData.toPercentage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.info
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Calendar.*
import kotlin.concurrent.fixedRateTimer

private val Int.toPositiveNumberOrZero: Int
    get() {
        return if (this >= 0) this else -this
    }

object GroupBotSuffix : KotlinPlugin(
    JvmPluginDescription(
        id = "com.pigeonyuze.group-bot-suffix",
        name = "GroupBotSuffix",
        version = "1.0.0",
    ) {
        author("鸽子宇泽")
        info("""自动设置bot在所有群聊的群名片信息，可用设置为倒计时以及相关内容""")
    }
) {
    private val botsList = mutableListOf<Bot>()

    override fun onEnable() {
        Config.reload()
        logger.info { "Plugin loaded" }
        GlobalEventChannel.subscribeAlways<BotOnlineEvent> {
            if (botsList.contains(this.bot)) return@subscribeAlways
            botsList.add(bot)
        }
        GlobalEventChannel.subscribeAlways<MessageEvent> {
            if (botsList.contains(this.bot)) botsList.remove(bot)
        }
        logger.info { "Try to start task" }

        fixedRateTimer(
            name = "SetNameTask",
            period = Config.waitTimeMS,
            initialDelay = Config.waitTimeMS
        ) {
            launch {
                for (bot in botsList) {
                    for (group in bot.groups) {
                        group.botAsMember.nameCard = bot.nick + Config.separator + parseContent()
                        delay(Config.waitGroupMS)
                    }
                }
            }.start()
            logger.debug("Set over.")
        }
        logger.info { "Start task!" }
    }

    private fun parseContent(): String{
        var waitForVar = false
        var ret = Config.content
        var arg = StringBuilder()
        for (char in Config.content) {
            if (char == '%') {
                waitForVar = true
                continue
            }
            if (!waitForVar) continue
            if (char == 's') {
                waitForVar = false
                ret = ret.replaceFirst("%s", parseContentImpl(Config.open,null))
                arg = arg.clear()
                continue
            }
            if (char == ' '){
                waitForVar = false
                ret = ret.replaceFirst("%$arg", parseContentImpl(Config.open,arg.toString()))
                arg = arg.clear()
                continue
            }
            arg.append(char)
        }
        return ret
    }

    private fun parseContentImpl(type: Config.SetNameType,arg: String?): String{
        return when(type){
            NOW_TIME -> {
                DateTimeFormatter.ofPattern(arg ?: "HH:mm:ss").format(LocalDateTime.now())
            }
            HOW_LONG_TO_DISTANCE -> { //默认按yyyy-MM-dd HH:mm:ss解析 默认为0
                arg!!

                //region Setting
                val now = GregorianCalendar()
                val dateSetting = arg.substring(0,9).split("-")
                var pattern = StringBuilder()

                var year = dateSetting[0].toInt()
                var month = dateSetting[1].toInt()
                pattern.append("yyyy年HH月dd日")
                var day = dateSetting.getOrNull(2)?.toInt() ?: kotlin.run{
                    month = year
                    year = now.get(YEAR)
                    pattern = pattern.clear().append("HH-dd")
                    dateSetting[1].toInt()
                }
                var hrs = 24
                var min = 0
                var sec = 0

                if (arg.length > 11) { //存在详细设定
                    pattern.append("HH小时mm分钟ss秒")
                    val timeSetting = arg.substring(10).split(":")
                    hrs = timeSetting.getOrNull(0)?.toInt() ?: 0

                    min = timeSetting.getOrNull(1)?.toInt() ?: 0
                    sec = timeSetting.getOrNull(2)?.toInt() ?: 0
                }
                //endregion

                //region Minus
                year = (year - now.get(YEAR)).toPositiveNumberOrZero
                month = (month - now.get(MONTH)).toPositiveNumberOrZero
                day = (day - now.get(DAY_OF_MONTH)).toPositiveNumberOrZero
                hrs = (hrs - now.get(HOUR_OF_DAY)).toPositiveNumberOrZero
                min = (min - now.get(MINUTE)).toPositiveNumberOrZero
                sec = (sec - now.get(SECOND)).toPositiveNumberOrZero

                pattern.toString()
                    .replace("yyyy", year.toString())
                    .replace("MM", month.toString())
                    .replace("dd", day.toString())
                    .replace("HH", hrs.toString())
                    .replace("mm", min.toString())
                    .replace("ss", sec.toString())
                //endregion
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
        }
    }
}