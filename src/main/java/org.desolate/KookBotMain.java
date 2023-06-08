package org.desolate;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import snw.jkook.command.JKookCommand;
import snw.jkook.entity.Guild;
import snw.jkook.message.component.card.CardBuilder;
import snw.jkook.message.component.card.MultipleCardComponent;
import snw.jkook.message.component.card.Size;
import snw.jkook.message.component.card.Theme;
import snw.jkook.message.component.card.element.PlainTextElement;
import snw.jkook.message.component.card.module.HeaderModule;
import snw.jkook.message.component.card.module.SectionModule;
import snw.jkook.plugin.BasePlugin;
import snw.jkook.scheduler.Scheduler;
import snw.jkook.scheduler.Task;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class KookBotMain extends BasePlugin {
    private static KookBotMain instance;
    private static Guild currentMessageGuild;
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

        //更新命令注册分类
        new JKookCommand("ServerInfo")
                .addPrefix("")
                .addOptionalArgument(String.class, "default")
                .executesUser((sender, arguments, message) -> {
                    String senderName = sender.getName();
                    //根命令获取服务器状态信息
                    if (Objects.equals(arguments[0], "default")) {
                        //调用查询方法(已封装)
                        JSONObject result = getMcServerDataPackAnalysis.getServerInfo();
                        if (!result.getJSONObject("status").getString("protocol").isEmpty()) {
                            String serverProtocol = "协议版本: " + result.getJSONObject("status").getString("protocol");
                            String serverVersion = "服务器版本: " + result.getJSONObject("status").getString("serverVersion");
                            String onlinePlayers = "在线玩家数/最大玩家数: " + result.getJSONObject("status").getString("onlinePlayers") + "/" + result.getJSONObject("status").getString("maxPlayers");
                            MultipleCardComponent ServerDataCard = new CardBuilder()
                                    .setTheme(Theme.PRIMARY)
                                    .setSize(Size.LG)
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC-Bot(ServerInfo)", false)))
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
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC-Bot(ServerInfo)", false)))
                                    .addModule(new HeaderModule(new PlainTextElement("错误: 服务器信息获取失败", false)))
                                    .build();
                            if (message != null) {
                                message.reply(ERRORCard);
                            }
                        }
                    } else if (arguments[0].toString().equalsIgnoreCase("Help")) {
                        MultipleCardComponent PlayerInfoCard = new CardBuilder()
                                .setTheme(Theme.PRIMARY)
                                .setSize(Size.LG)
                                .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC-Bot(Help)", false)))
                                .addModule(new SectionModule(new PlainTextElement("/ServerInfo - 反馈服务器当前的信息"), null, null))
                                .addModule(new SectionModule(new PlainTextElement("/ServerInfo SelfPVP - 查询自己的PVP数据(需绑定游戏账户)"), null, null))
                                .addModule(new SectionModule(new PlainTextElement("/ServerInfo bind {PlayerName} - 绑定自己的游戏账户\nTips: 请注意绑定时您需要在游戏内"), null, null))
                                .build();
                    } else if (arguments[0].toString().equalsIgnoreCase("bind") && arguments.length >= 2) {
                        String PlayerName = (String) arguments[1];
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
                                if (message != null) {
                                    message.reply("您已经绑定过游戏账户了哦，不能再次绑定了！");
                                }
                            }
                        } else {
                            if (message != null) {
                                message.reply("请检查玩家ID是否正确或玩家是否在游戏内！");
                            }
                        }
                    } else if (arguments[0].toString().equalsIgnoreCase("SelfPVP")) {
                        if (!jsonFileOperate.IfPlayerIsNoBinding(sender.getId())) {
                            //玩家已绑定
                            JSONObject currentPlayerInfo = jsonFileOperate.getCurrentClassPlayerInfo();
                            String cPlayerName = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".name");
                            String cPlayerKills = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".kills");
                            String cPlayerDeaths = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".deaths");
                            String cPlayerStreak = analysisYmlFile.getYmlValue(currentPlayerInfo.getString("playerUUID") + ".streak");
                            double kill = Double.parseDouble(cPlayerKills);
                            double death = Double.parseDouble(cPlayerDeaths);
                            double ratio = kill / death == 0 ? 1 : death;
                            String PlayerRatio = String.format("%.2f", ratio);
                            //构建卡片信息
                            MultipleCardComponent PlayerInfoCard = new CardBuilder()
                                    .setTheme(Theme.PRIMARY)
                                    .setSize(Size.LG)
                                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-PVP-DataQuery", false)))
                                    .addModule(new SectionModule(new PlainTextElement("游戏昵称: " + cPlayerName, false), null, null))
                                    .addModule(new SectionModule(new PlainTextElement("击杀: " + cPlayerKills, false), null, null))
                                    .addModule(new SectionModule(new PlainTextElement("死亡: " + cPlayerDeaths, false), null, null))
                                    .addModule(new SectionModule(new PlainTextElement("Ratio: " + PlayerRatio, false), null, null))
                                    .addModule(new SectionModule(new PlainTextElement("最高连杀: " + cPlayerStreak, false), null, null))
                                    .build();
                            if (message != null) {
                                message.reply(PlayerInfoCard);
                            }
                        } else {
                            if (message != null) {
                                message.reply("检测到你没有绑定游戏账户哦，快快去绑定一个叭！");
                            }
                        }
                    } else if (arguments[0].toString().equalsIgnoreCase("changeBind") && arguments.length >= 2) {

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

    public static void setCurrentMessageGuild(Guild currentMessageGuild) {
        KookBotMain.currentMessageGuild = currentMessageGuild;
    }
}