package com.pigeonyuze

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("Setting") {
    @ValueDescription("""
        每次间隔的时间 单位为毫秒
        不建议将此项设得过快 因为mirai并不主动推送群bot名片修改的事件(用户查询bot群名片/发送信息时才可能修改) 过快并不一定有效
    """)
    val waitTimeMS: Long by value(60_000L)

    @ValueDescription(
        """
        选择的方法，可选以下内容 
        NOW_TIME (现在的时间 可提供参数自行设置 默认为HH:mm:ss)
        HOW_LONG_TO_DISTANCE_DETAIL (距离什么时候还有多久 需要用参数提供指定日期(yyyy-MM-dd HH:mm:ss) 默认HH:mm:ss会设置为0:0:0),
        CPU_LOAD (系统cpu占用率),
        JVM_CPU_LOAD (jvm可使用的cpu占用率),
        MEMORY_LOAD (系统内存占用率),
        JVM_MEMORY_LOAD (jvm可使用内存占用率)
        """
    )
    val open: SetNameType by value(SetNameType.NOW_TIME)

    @ValueDescription("""
        设置的名片后缀 使用%xxx来代替对应的值
        使用%s为默认参数
        也可用%xxx ('%'后面为参数 直到空格结束)
    """)
    val content: String by value("现在是北京时间 %s !")

    @ValueDescription("""
        bot名称与后缀的分割符号
    """)
    val separator: String by value(" | ")

    @ValueDescription(
        """
            经过多少毫秒后修改下一个群聊的群名片
            (如果为0的话就不会等待)
        """)
    val waitGroupMS: Long by value(100L)

    @ValueDescription("""
        只在以下群聊开启
    """)
    val allowlist: List<Long> by value(listOf(114514L))

    @ValueDescription("""
        修改群名时使用的模式
        
        - LAZY 惰性修改群名，仅当 Bot 主动发出信息时才会可能修改群名
        - RUNTIME (不推荐) 自成功加载插件后，每过指定的时间就会主动更改群名
        
        注： 使用 `RUNTIME` 模式可能会导致被 mirai 服务器断开连接
        详情 -> https://github.com/PigeonYuze/GroupBotSuffix/issues/11
    """)
    val runningMode: Mode by value(Mode.LAZY)

    val isModeLazy by lazy {
        runningMode == Mode.LAZY
    }

    @Suppress("unused")
    enum class Mode {
        LAZY,
        RUNTIME
    }

    enum class SetNameType{
        NOW_TIME,
        HOW_LONG_TO_DISTANCE_DETAIL,
        /* 向下兼容，保存时进行切换 */
        HOW_LONG_TO_DISTANCE,
        HOW_LONG_TO_DISTANCE_SIMPLE,
        CPU_LOAD,
        JVM_CPU_LOAD,
        MEMORY_LOAD,
        JVM_MEMORY_LOAD,
        RANDOM_TEXT;
    }
}
