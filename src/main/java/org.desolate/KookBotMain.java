package org.desolate;

import com.alibaba.fastjson2.JSONArray;
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

import java.util.Objects;

public class KookBotMain extends BasePlugin {
    private static KookBotMain instance;
    private static final GetMcServerDataPackAnalysis getMcServerDataPackAnalysis = new GetMcServerDataPackAnalysis();
    private static final AnalysisYmlFile analysisYmlFile = new AnalysisYmlFile();
    private static final JsonFileOperate jsonFileOperate = new JsonFileOperate();

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
        analysisYmlFile.setYmlFilePath(getConfig().getString("YmlFilePath"));
        jsonFileOperate.setDataFolderPath(getDataFolder().getPath());
        jsonFileOperate.DatabaseFileInit();

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
                                .addModule(new SectionModule(new PlainTextElement("/查询 服务器信息 - 反馈服务器当前的信息"), null, null))
                                .addModule(new SectionModule(new PlainTextElement("/查询 PVP数据 - 查询自己的PVP数据情况"), null, null))
                                .build();
                        if (message != null) {
                            message.reply(NoneReplyCard);
                        }
                    } else if (arguments[0].equals("服务器信息")) {
                        //调用查询方法(已封装)
                        JSONObject result = getMcServerDataPackAnalysis.getServerInfo();
                        if (!result.getJSONObject("status").getString("protocol").isEmpty()) {
                            String serverProtocol = "协议版本: " + result.getJSONObject("status").getString("protocol");
                            String serverVersion = "服务器版本: " + result.getJSONObject("status").getString("serverVersion");
                            String onlinePlayers = "在线玩家数/最大玩家数: " + result.getJSONObject("status").getString("onlinePlayers") + "/" + result.getJSONObject("status").getString("maxPlayers");
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
                            MultipleCardComponent ERRORCard = new CardBuilder()
                                    .setTheme(Theme.PRIMARY)
                                    .setSize(Size.LG)
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC Server", false)))
                                    .addModule(new HeaderModule(new PlainTextElement("错误: 服务器信息获取失败", false)))
                                    .build();
                            if (message != null) {
                                message.reply(ERRORCard);
                            }
                        }
                    } else if (arguments[0].equals("PVP数据")) {
                        if (!jsonFileOperate.IfPlayerIsNoBinding(sender.getId())) {
                            //玩家已绑定
                            JSONObject currentPlayerInfo = jsonFileOperate.getCurrentClassPlayerInfo();
                            String cPlayerName = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".name");
                            String cPlayerKills = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".kills");
                            String cPlayerDeaths = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".deaths");
                            String cPlayerStreak = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".streak");
                            //构建卡片信息
                            MultipleCardComponent PlayerInfoCard = new CardBuilder()
                                    .setTheme(Theme.PRIMARY)
                                    .setSize(Size.LG)
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-PVP-DataQuery", false)))
                                    .addModule(new HeaderModule(new PlainTextElement("游戏昵称: " + cPlayerName, false)))
                                    .addModule(new HeaderModule(new PlainTextElement("击杀数量: " + cPlayerKills, false)))
                                    .addModule(new HeaderModule(new PlainTextElement("死亡次数: " + cPlayerDeaths, false)))
                                    .addModule(new HeaderModule(new PlainTextElement("运气率: " + cPlayerStreak, false)))
                                    .build();
                            if (message != null) {
                                message.reply(PlayerInfoCard);
                            }
                        } else {
                            message.reply("检测到你没有绑定游戏账户哦，快快去绑定一个叭！");
                        }
                    } else {
                        if (message != null) {
                            message.reply("None");
                        }
                    }
                }).register(this);
        //注册命令 -- 获取玩家数据
        new JKookCommand("绑定")
                .addOptionalArgument(String.class, "None")
                .executesUser((sender, arguments, message) -> {
                    String senderName = sender.getName();
                    if (arguments.length >= 1 & arguments[0] == "None") {
                        if (message != null) {
                            message.reply(senderName + " 请输入/绑定 [玩家游戏昵称] 来将你的KOOK和服务器绑定，请注意绑定时您需要在游戏内！");
                        }
                    } else {
                        String PlayerName = (String) arguments[0];
                        String PlayerUUID = "00000000-0000-0000-0000-000000000000";
                        //获取数据
                        JSONObject jsonDataPack = getMcServerDataPackAnalysis.getServerInfo();
                        JSONArray jsonArrayList = JSONArray.parse(jsonDataPack.getJSONObject("onlinePlayerList").getString("playerList"));
                        for (Object item : jsonArrayList) {
                            JSONObject temp = JSONObject.parse(item.toString());
                            if (Objects.equals(temp.getString("name"), PlayerName)) {
                                PlayerUUID = temp.getString("id");
                                break;
                            }
                        }
                        //判玩家是否在线
                        if (!Objects.equals(PlayerUUID, "00000000-0000-0000-0000-000000000000")) {
                            //判玩家是否已绑定
                            if (jsonFileOperate.IfPlayerIsNoBinding(sender.getId())) {
                                //添加新玩家绑定
                                if (jsonFileOperate.AddNewBinding(PlayerName, PlayerUUID, sender.getId())) {
                                    MultipleCardComponent BindCard = new CardBuilder()
                                            .setTheme(Theme.PRIMARY)
                                            .setSize(Size.LG)
                                            .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC Server", false)))
                                            .addModule(new SectionModule(new PlainTextElement("KOOK用户: " + senderName + "\n游戏账户: " + PlayerName + "\n绑定成功!"), null, null))
                                            .build();
                                    if (message != null) {
                                        message.reply(BindCard);
                                    }
                                }
                            } else {
                                message.reply("您已经绑定过游戏账户了哦，不能再次绑定了！");
                            }
                        } else {
                            if (message != null) {
                                message.reply("请检查玩家ID是否正确或玩家是否在游戏内！");
                            }
                        }
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