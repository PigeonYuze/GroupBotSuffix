# GroupBotSuffix
基于mirai的自动增加qqbot群名后缀的插件

通过本插件，你可以实现在bot的每一个群聊都自动修改群名片为含后缀 (如现在是北京时间xx:xx此类的表达)


## 如何使用?

将 releases 下的最新包下载，随后丢到`plugins`里面

随后打开mcl在加载完毕后停止，打开`config`页面，修改本插件的`Setting.yml`

默认会在成功加载后每隔一分钟修改一次

## 配置说明

- `waitTimeMS`
  每次间隔的时间 单位为毫秒<br>
  不建议将此项设得过快 因为mirai并**不主动推送**群bot名片修改的事件(用户查询bot群名片/发送信息时才可能修改) **过快并不一定有效**--
- `open`
  添加后缀类型的值<br>
  可以为以下内容
   - `NOW_TIME` 现在的时间 可提供参数自行设置 默认为HH:mm:ss
   - `HOW_LONG_TO_DISTANCE` 距离什么时候还有多久 需要用参数提供指定日期<br>年月日用`-`分割，且位于开头(可不提供年) 时分秒用`:`分割(可不提供)<br> 返回的格式与提供的格式一致
   - `CPU_LOAD` 系统cpu占用率
   - `JVM_CPU_LOAD` jvm可使用的cpu占用率
   - `MEMORY_LOAD` 系统内存占用率
   - `JVM_MEMORY_LOAD` jvm可使用内存占用率
- `content` 
  后缀的内容
  使用`%s`为默认配置的调用
  使用`%`加参数内容为有参数的配置调用
- `separator`
  `bot`昵称到后缀的分割(默认会重命名为 `bot` 名称 + 后缀分隔符 + 后缀内容)
- `waitGroupMS` 
  经过多少毫秒后修改下一个群聊的群名片 单位为毫秒
   
