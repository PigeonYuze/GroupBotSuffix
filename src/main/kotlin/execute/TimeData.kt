package com.pigeonyuze.execute

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TimeData {
    fun getHowLongToDistanceSimpleImpl(arg: String): String {
        val objLocalDateTime = LocalDateTime.parse(
            arg,
            DateTimeFormatter.ofPattern(if (arg.length == 10) "yyyy-MM-dd" else "yyyy-MM-dd HH:mm:ss")
        )
        val nowLocalDateTime = LocalDateTime.now()
        val duration = Duration.between(
            nowLocalDateTime, objLocalDateTime
        )
        val days = duration.toDays()
        val hour = duration.toHours() % 24
        val minute = duration.toMinutes() % 60
        return "${days}天${hour}小时${minute}分钟"
    }

    fun getHowLongToDistanceDetailImpl(arg: String): String {
        //region Setting
        val now = LocalDateTime.now()
        val dateSetting = arg.substring(0, 8).split("-")
        var pattern = StringBuilder()

        var year = dateSetting[0].toInt()
        var month = dateSetting[1].toInt()
        println(dateSetting)
        pattern.append("yyyy年HH月dd日")
        val day = dateSetting.getOrNull(2)?.toInt() ?: kotlin.run {
            month = year
            year = now.year
            pattern = pattern.clear().append("HH-dd")
            dateSetting[1].toInt()
        }
        var hrs = 24
        var min = 0
        var sec = 0

        if (arg.length > 11) { //存在详细设定
            pattern.append("HH小时mm分钟ss秒")
            val timeSetting = arg.substring(9).split(":")
            println(timeSetting)
            hrs = timeSetting.getOrNull(0)?.toInt() ?: 0

            min = timeSetting.getOrNull(1)?.toInt() ?: 0
            sec = timeSetting.getOrNull(2)?.toInt() ?: 0
        }
        //endregion
        val objDateTime = LocalDateTime.of(
            LocalDate.of(
                year,
                month,
                day
            ),
            LocalTime.of(
                hrs,
                min,
                sec
            )
        )
        val duration = Duration.between(now, objDateTime)
        val result = now.minus(duration)
        return result.format(DateTimeFormatter.ofPattern(pattern.toString()))
        //endregion
    }

    fun getHowLongToDistanceImpl(arg: String) = getHowLongToDistanceDetailImpl(arg)

    private inline fun Int.ifTrue(boolean: Boolean,run: (Int) -> Int) : Int{
        return if (boolean) run.invoke(this)
        else this
    }

    private fun createRange(from: Int,to: Int) = if (to > from) from until to else to until from

    private val Int.toPositiveNumberOrZero: Int
        get() = if (this >= 0) this else -this
}