package com.pigeonyuze.command

import com.pigeonyuze.Config
import com.pigeonyuze.GroupBotSuffix
import com.pigeonyuze.GroupBotSuffix.reload
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

object SetNamePermissionCommand : CompositeCommand(
    GroupBotSuffix, "suffix", description = "对 GroupBotSuffix 的相关设置"
) {
    @SubCommand("reload")
    @Description("重载插件设置")
    suspend fun CommandSender.reload() {
        Config.reload()
        sendMessage("Done.")
    }

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand("add", "permit", "grant", "allow")
    @Description("查看被授权权限列表")
    suspend fun CommandSender.add(@Name("被许可群聊 ID") qqid: Long) {
        GroupBotSuffix.allowlist.allow(qqid)
        sendMessage("Done.")
    }

    @OptIn(ConsoleExperimentalApi::class)
    @SubCommand("cancel", "dey", "rm", "ban")
    @Description("撤销一个权限")
    suspend fun CommandSender.cancel(@Name("被许可群聊 ID") qqid: Long) {
        GroupBotSuffix.allowlist.ban(qqid)
        sendMessage("Done.")
    }

    @SubCommand("allOpen", "ap")
    @Description("对所有群聊都打开功能")
    suspend fun CommandSender.allOpen() {
        GroupBotSuffix.allowlist.allowAll()
        sendMessage("Done.")
    }

    @SubCommand
    @Description("删除 `allowList` 的所有内容")
    suspend fun CommandSender.reset() {
        GroupBotSuffix.allowlist.reset()
        sendMessage("Done.")
    }

    @SubCommand("listPermission", "lp")
    @Description("查看权限被授权群聊列表")
    suspend fun CommandSender.listPermission() {
        val pluginPermission = GroupBotSuffix.allowlist

        val sb = StringBuilder("-\n")
        sb.append("|- com.pigeonyuze.GroupBotSuffix\n")
        sb.append("|-- white\n")
        for (groupId in pluginPermission.white) {
            sb.append("|--- g")
            sb.append(groupId)
            sb.append('\n')
        }
        if (pluginPermission.isAllowAll) {
            sb.append("|--- g*\n")
        }
        sb.append("|-- black\n")
        for (groupId in pluginPermission.black) {
            sb.append("|--- g")
            sb.append(groupId)
            sb.append('\n')
        }
        sb.deleteCharAt(sb.lastIndex)
        sendMessage(sb.toString())
    }

    @SubCommand("listWhite", "lw")
    @Description("查询被许可的群聊")
    suspend fun CommandSender.listWhite() {
        sendMessage(
            GroupBotSuffix.allowlist.white
                .joinToString(separator = "\n", prefix = "|- White List\n") { "|-- $it" }
                .plus(if (GroupBotSuffix.allowlist.isAllowAll) "\n|-- g*" else "")
        )
    }

    @SubCommand("listBlack", "lb")
    @Description("查询被许可的群聊")
    suspend fun CommandSender.listBlack() {
        sendMessage(
            GroupBotSuffix.allowlist.black
                .joinToString(separator = "\n", prefix = "|- Black List\n") { "|-- $it" }
        )
    }
}