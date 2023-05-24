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
    private static GetMcServerDataPackAnalysis getMcServerDataPackAnalysis = new GetMcServerDataPackAnalysis();

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
        //注册命令
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
                        message.reply(NoneReplyCard);
                    } else if (arguments[0].equals("服务器信息")) {
                        //调用查询方法(已封装)
                        JSONObject result = getMcServerDataPackAnalysis.getServerInfo();
//                        MyLogger(getMcServerDataPackAnalysis.getServerAddress());
//                        MyLogger(Integer.toString(getMcServerDataPackAnalysis.getServerPort()));
                        String serverProtocol = "协议版本:" + result.getString("protocol").toString();
                        String serverVersion = "服务器版本:" + result.getString("serverVersion").toString();
                        String onlinePlayers = "在线玩家数/最大玩家数:" + result.getString("onlinePlayers").toString()+"/"+ result.getString("maxPlayers").toString();
                        MultipleCardComponent ServerDataCard = new CardBuilder()
                                .setTheme(Theme.PRIMARY)
                                .setSize(Size.LG)
                                .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC Server", false)))
//                                .addModule(new SectionModule(new PlainTextElement(result.toString()), null, null))
                                .addModule(new SectionModule(new PlainTextElement(serverProtocol), null, null))
                                .addModule(new SectionModule(new PlainTextElement(serverVersion), null, null))
//                                .addModule(new SectionModule(new PlainTextElement("motd"), null, null))
                                .addModule(new SectionModule(new PlainTextElement(onlinePlayers), null, null))
                                .build();
                        message.reply(ServerDataCard);
                    } else {
                        message.reply("None");
                    }
                }).register(this);
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