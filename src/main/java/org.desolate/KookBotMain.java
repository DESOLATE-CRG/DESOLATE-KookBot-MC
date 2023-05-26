package org.desolate;

import com.alibaba.fastjson2.JSONObject;
import snw.jkook.command.JKookCommand;
import snw.jkook.message.component.card.CardBuilder;
import snw.jkook.message.component.card.MultipleCardComponent;
import snw.jkook.message.component.card.Size;
import snw.jkook.message.component.card.Theme;
import snw.jkook.message.component.card.element.PlainTextElement;
import snw.jkook.message.component.card.module.HeaderModule;
import snw.jkook.message.component.card.module.SectionModule;
import snw.jkook.plugin.BasePlugin;

public class KookBotMain extends BasePlugin {
    private static KookBotMain instance;
    private static final GetMcServerDataPackAnalysis getMcServerDataPackAnalysis = new GetMcServerDataPackAnalysis();

    @Override
    public void onLoad() {
        instance = this;
        //在插件加载时释放配置文件
        saveDefaultConfig();
        MyLogger("插件正在加载...");
    }

    @Override
    public void onEnable() {
        //设置全局配置
        getMcServerDataPackAnalysis.setServerAddress(getConfig().getString("ServerIPAddress"));
        getMcServerDataPackAnalysis.setServerPort(getConfig().getInt("ServerPort"));
        //注册命令 -- 查询服务器状态
        new JKookCommand("查询")
                .addOptionalArgument(String.class, "None")
                .executesUser((sender, arguments, message) -> {
                    String senderName = sender.getName();
                    if (arguments.length >= 1 & arguments[0] == "None") {
                        MultipleCardComponent NoneReplyCard = new CardBuilder()
                                .setTheme(Theme.PRIMARY)
                                .setSize(Size.LG)
                                .addModule(new HeaderModule(new PlainTextElement(senderName + "你想要查询什么呢？", false)))
                                .addModule(new SectionModule(new PlainTextElement("使用方法："), null, null))
                                .addModule(new SectionModule(new PlainTextElement("/查询 服务器信息-反馈服务器当前的信息"), null, null))
                                .build();
                        if (message != null) {
                            message.reply(NoneReplyCard);
                        }
                    } else if (arguments[0].equals("服务器信息")) {
                        //调用查询方法(已封装)
                        JSONObject result = getMcServerDataPackAnalysis.getServerInfo();
                        if (!result.getString("protocol").isEmpty()) {
                            String serverProtocol = "协议版本:" + result.getString("protocol");
                            String serverVersion = "服务器版本:" + result.getString("serverVersion");
                            String onlinePlayers = "在线玩家数/最大玩家数:" + result.getString("onlinePlayers") + "/" + result.getString("maxPlayers");
                            MultipleCardComponent ServerDataCard = new CardBuilder()
                                    .setTheme(Theme.PRIMARY)
                                    .setSize(Size.LG)
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC Server", false)))
                                    .addModule(new SectionModule(new PlainTextElement(serverProtocol), null, null))
                                    .addModule(new SectionModule(new PlainTextElement(serverVersion), null, null))
                                    .addModule(new SectionModule(new PlainTextElement(onlinePlayers), null, null))
                                    .build();
                            if (message != null) {
                                message.reply(ServerDataCard);
                            }
                        } else {
                            MultipleCardComponent multipleCardComponent = new CardBuilder()
                                    .setTheme(Theme.PRIMARY)
                                    .setSize(Size.LG)
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC Server", false)))
                                    .addModule(new HeaderModule(new PlainTextElement("错误: 服务器信息获取失败", false)))
                                    .build();
                            if (message != null) {
                                message.reply(multipleCardComponent);
                            }
                        }
                    } else {
                        if (message != null) {
                            message.reply("None");
                        }
                    }
                }).register(this);
        //注册命令 -- 获取玩家数据
        MyLogger("插件加载成功");
    }

    @Override
    public void onDisable() {
        MyLogger("插件正在关闭");
    }

    public static void MyLogger(String s) {
        getInstance().getLogger().info("[GetMS] " + s);
    }

    public static KookBotMain getInstance() {
        return instance;
    }
}