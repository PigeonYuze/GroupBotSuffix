package com.pigeonyuze

import com.pigeonyuze.GroupBotSuffix.logger
import java.lang.NumberFormatException

class PluginPermission(private val exprs: MutableList<String>) {
    val white = mutableListOf<Long>()
    val black = mutableListOf<Long>()
    private var isAllowAll0 = false


    init {
        for (expr in exprs) {
            try {
                handleEachExpr(expr)
            } catch (e: NumberFormatException) {
                logger.error("无效的值, 目标不是纯数字 -> ${expr.drop(1)}")
            }
        }
        logger.info("成功解析权限设置")
        if (isAllowAll0) logger.info("白名单: 全部群聊") else logger.info("白名单: $white")
        if (black.isEmpty()) logger.info("黑名单: 无") else logger.info("黑名单: $black")
    }

    fun allow(groupId: Long) {
        if (!black.remove(groupId)) {
            white.add(groupId)
            exprs.add("+$groupId")
        }
        exprs.remove("-$groupId")
    }

    fun ban(groupId: Long) {
        if (!white.remove(groupId)) {
            black.add(groupId)
            exprs.add("-$groupId")
        }
        exprs.remove("+$groupId")
    }

    fun reset() {
        exprs.clear()
    }

    fun allowAll() {
        val iterator = exprs.iterator()
        while (iterator.hasNext()) {
            val content = iterator.next()
            if (content.startsWith('+')) {
                iterator.remove()
            }
        }
        exprs.add("*")
    }


    fun hasPermission(objId: Long): Boolean {
        return if (isAllowAll0) {
            !black.contains(objId)
        } else {
            white.contains(objId)
        }
    }

    val isAllowAll: Boolean
        get() {
            return isAllowAll0
        }

    private fun handleEachExpr(expr: String) {
        when (expr.first()) {
            '-' -> {
                val groupId = expr.drop(1).toLong()
                if (white.contains(groupId)) {
                    white.remove(groupId)
                } else {
                    black.add(groupId)
                }
            }

            '+' -> {
                val groupId = expr.drop(1).toLong()
                if (black.contains(groupId)) {
                    black.remove(groupId)
                } else {
                    white.add(groupId)
                }
            }

            '*' -> {
                isAllowAll0 = true
            }

            in '0'..'9' -> {
                white.add(expr.toLong())
            }
        }
    }
}