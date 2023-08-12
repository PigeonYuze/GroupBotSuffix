package com.pigeonyuze

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("Setting") {


    @ValueDescription(
        """
        修改群名时使用的模式
        
        - LAZY 惰性修改群名，仅当 Bot 主动发出信息时才会可能修改群名
        - RUNTIME (不推荐) 自成功加载插件后，每过指定的时间就会主动更改群名
        
        注： 使用 `RUNTIME` 模式可能会导致被 mirai 服务器断开连接
        详情 -> https://github.com/PigeonYuze/GroupBotSuffix/issues/11
    """
    )
    val mode: Mode by value(Mode.LAZY)

    @ValueDescription(
        """
        更改完全部群聊后等待的时间 单位为毫秒
        
        当 mode 为 LAZY 时，至少需要等待以下时间才会修改一次
       
        每分钟最多累计修改30次，超过30次后将会忽略修改名称的请求.
    """
    )
    val waitTimeMS: Long by value(60_000L)

    @ValueDescription(
        """
            更改单个群聊后等待时间 单位为毫秒
            
            此项在当 mode 为 LAZY 时无效.
            
            必须大于或等于 150 ms 且 小于 waitTimeMs 中设置的值
        """
    )
    val waitGroupMS: Long by value(300L)

    @ValueDescription(
        """
        仅在 mode 为 RUNTIME 的环境下生效
        
        当此项开启后，等待的时间为原有的时间为最小值区限内的随机值
        
        强烈建议开启此项，这样可以使此行为不算那么的“可疑”
        
        举个例子：
         在 waitTimeMS 为 60000 且 waitGroupMS 为 300 时
         每一轮修改名称的等待时间为 60000(waitTimeMS) 到 70000(+1000) 之间的随机数
         每一个群聊之间的间隔为 300(waitGroupMS) 到 400(+100(< 60000)) 之间的随机数
    """
    )
    val isOpenRandomTime: Boolean by value(true)

    @ValueDescription(
        """
        选择的方法，可选以下内容 :
        
        - NOW_TIME 
            (现在的时间 可提供参数自行设置 默认为HH:mm:ss)
        - HOW_LONG_TO_DISTANCE_SIMPLE 
            (距离什么时候还有多久 需要用参数提供指定日期(yyyy-MM-dd HH:mm:ss) 返回 天数/小时/分钟)
        - HOW_LONG_TO_DISTANCE_DETAIL 
            (距离什么时候还有多久 需要用参数提供指定日期(yyyy-MM-dd HH:mm:ss) 默认HH:mm:ss会设置为0:0:0),
        - CPU_LOAD 
            (系统cpu占用率),
        - JVM_CPU_LOAD 
            (jvm可使用的cpu占用率),
        - MEMORY_LOAD 
            (系统内存占用率),
        - JVM_MEMORY_LOAD 
            (jvm可使用内存占用率)
        """
    )
    val open: SetNameType by value(SetNameType.NOW_TIME)

    @ValueDescription(
        """
        设置的名片后缀 使用%xxx来代替对应的值
        使用%s为默认参数
        也可用%xxx ('%'后面为参数 直到空格结束)
    """
    )
    val content: String by value("现在是北京时间 %s !")

    @ValueDescription(
        """
        bot名称与后缀的分割符号
    """
    )
    val separator: String by value(" | ")


    @ValueDescription(
        """
        只在以下群聊开启
    """
    )
    val allowlist: List<Long> by value(listOf(114514L))

    // Fields ^
    /////////////////////////////////////////////////////////////
    // Not for user fields
    ////////////////////////////////////////////////////////////


    val isModeLazy by lazy {
        mode == Mode.LAZY
    }

    val waitTimeRange: Array<LongRange> by lazy {
        if (!isOpenRandomTime && mode == Mode.LAZY) {
            return@lazy arrayOf(
                waitTimeMS..waitTimeMS,
                waitGroupMS..waitGroupMS
            )
        }
        if (waitGroupMS <= 150) {
            throw IllegalArgumentException(
                "The `waitGroupMS` field value should be a number greater than or equal to 150, but it is actually $waitTimeMS"
            )
        }
        if (waitGroupMS >= waitTimeMS - 500) {
            throw IllegalArgumentException(
                "The `waitGroupMS` field value should be a number less 500 than waitTimeMS(${waitTimeMS - 500}), but it is actually $waitTimeMS"
            )
        }

        arrayOf(
            waitTimeMS..waitTimeMS + 1_000,
            waitGroupMS..waitGroupMS + 100
        )
    }

    const val WAIT_TIME_RANGE_INDEX = 0
    const val WAIT_GROUP_RANGE_INDEX = 1


    @Suppress("unused")
    enum class Mode {
        LAZY,
        RUNTIME
    }

    enum class SetNameType {
        NOW_TIME,
        HOW_LONG_TO_DISTANCE_DETAIL,
        HOW_LONG_TO_DISTANCE_SIMPLE,
        CPU_LOAD,
        JVM_CPU_LOAD,
        MEMORY_LOAD,
        JVM_MEMORY_LOAD,
        RANDOM_TEXT;
    }
}
