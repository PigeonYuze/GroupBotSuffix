package com.pigeonyuze.execute

import oshi.SystemInfo
import java.lang.management.ManagementFactory

object SystemData {

    val jvmMemory: Double get() {
        val runtime = Runtime.getRuntime()

        val max = runtime.maxMemory()
        val used = runtime.totalMemory()
        return (used.toFloat() / max.toFloat()).toDouble()
    }

    val systemMemory: Double get() {
        val bean = SystemInfo().hardware.memory
        val used = bean.available //non-zero
        val max = bean.total //non zero
        return (used.toFloat() / max.toFloat()).toDouble()
    }

    val jvmCpuLoad: Double get() {
        val pid = ManagementFactory.getRuntimeMXBean().pid.toInt()
        val process = SystemInfo().operatingSystem.getProcess(pid)
        return process.processCpuLoadCumulative
    }

    val systemCpuLoad: Double get() {
        val delay = 1_000L
        val bean = SystemInfo().hardware.processor
        return bean.getSystemCpuLoad(delay)
    }

    fun Double.toPercentage(): String {
        val str = (this * 100).toString()
        val ret: String = if (str.length > 5) str.substring(0,5)
        else str.padEnd(5,'0')
        return ret.plus("%")
    }
}