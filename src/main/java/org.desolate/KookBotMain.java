package org.desolate;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import snw.jkook.command.JKookCommand;
import snw.jkook.entity.User;
import snw.jkook.message.Message;
import snw.jkook.message.component.card.CardBuilder;
import snw.jkook.message.component.card.MultipleCardComponent;
import snw.jkook.message.component.card.Size;
import snw.jkook.message.component.card.Theme;
import snw.jkook.message.component.card.element.ButtonElement;
import snw.jkook.message.component.card.element.InteractElement;
import snw.jkook.message.component.card.element.PlainTextElement;
import snw.jkook.message.component.card.module.ActionGroupModule;
import snw.jkook.message.component.card.module.ContextModule;
import snw.jkook.message.component.card.module.HeaderModule;
import snw.jkook.message.component.card.module.SectionModule;
import snw.jkook.plugin.BasePlugin;

import java.util.*;

public class KookBotMain extends BasePlugin {
    private static KookBotMain instance;
    private static final GetMcServerDataPackAnalysis getMcServerDataPackAnalysis = new GetMcServerDataPackAnalysis();
    public static final AnalysisYmlFile analysisYmlFile = new AnalysisYmlFile();
    private static final JsonFileOperate jsonFileOperate = new JsonFileOperate();
    public Map<User, Integer> userPages = new HashMap<>();
    private static final int PAGE_SIZE = 10;

    @Override
    public void onLoad() {
        instance = this;
        MyLogger("插件开始加载...");
        //在插件加载时释放配置文件
        saveDefaultConfig();
        //释放权限配置文件
        saveResource("Administrator.json", false, false);
    }

    @Override
    public void onEnable() {
        // 设置全局配置
        setupGlobalConfig();

        // 更新命令注册分类
        registerCommands();
        getCore().getEventManager().registerHandlers(this, new EventListener());

        MyLogger("插件加载成功");
    }

    private void setupGlobalConfig() {
        getMcServerDataPackAnalysis.setServerAddress(getConfig().getString("ServerIPAddress"));
        getMcServerDataPackAnalysis.setServerPort(getConfig().getInt("ServerPort"));
        analysisYmlFile.setYmlFilePath(getConfig().getString("PVPStateYmlFilePath"), getConfig().getString("ItemKillState"));
        jsonFileOperate.setDataFolderPath(getDataFolder().getPath());
        jsonFileOperate.DatabaseFileInit();
    }

    private void registerCommands() {
        new JKookCommand("ServerInfo")
                .addPrefix("")
                .addOptionalArgument(String.class, "default")
                .executesUser(this::handleCommand)
                .register(this);

        new JKookCommand("cx")
                .addPrefix("")
                .addOptionalArgument(String.class, "default")
                .executesUser(this::handleCxCommand)
                .register(this);
    }

    private void handleCommand(User sender, Object[] args, Message message) {
        List<Object> arguments = Arrays.asList(args);
        String command = (String) arguments.get(0);

        switch (command.toLowerCase()) {
            case "default":
                handleDefaultCommand(message);
                break;
            case "help":
                handleHelpCommand(message);
                break;
            case "bind":
                if (arguments.size() >= 2) {
                    handleBindCommand(sender, (String) arguments.get(1), message);
                }
                break;
            case "selfpvp":
                handleSelfPVPCommand(sender, message);
                break;
            case "changebind":
                if (arguments.size() >= 3) {
                    handleChangeBindCommand(sender, (String) arguments.get(1), (String) arguments.get(2), message);
                }
                break;
            default:
                message.reply("未知命令，请输入 /ServerInfo Help 查看使用帮助。");
                break;
        }
    }

    private void handleDefaultCommand(Message message) {
        JSONObject result = getMcServerDataPackAnalysis.getServerInfo();
        MultipleCardComponent serverDataCard;

        if (!result.getJSONObject("status").getString("protocol").isEmpty()) {
            serverDataCard = new CardBuilder()
                    .setTheme(Theme.PRIMARY)
                    .setSize(Size.LG)
                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC-Bot(ServerInfo)", false)))
                    .addModule(new SectionModule(new PlainTextElement("协议版本: " + result.getJSONObject("status").getString("protocol")), null, null))
                    .addModule(new SectionModule(new PlainTextElement("服务器版本: " + result.getJSONObject("status").getString("serverVersion")), null, null))
                    .addModule(new SectionModule(new PlainTextElement("在线玩家数/最大玩家数: " + result.getJSONObject("status").getString("onlinePlayers") + "/" + result.getJSONObject("status").getString("maxPlayers")), null, null))
                    .addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))))
                    .build();
        } else {
            serverDataCard = new CardBuilder()
                    .setTheme(Theme.PRIMARY)
                    .setSize(Size.LG)
                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC-Bot(ServerInfo)", false)))
                    .addModule(new HeaderModule(new PlainTextElement("错误: 服务器信息获取失败", false)))
                    .addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))))
                    .build();
        }

        if (message != null) {
            message.reply(serverDataCard);
        }
    }

    private void handleHelpCommand(Message message) {
        MultipleCardComponent helpInfoCard = new CardBuilder()
                .setTheme(Theme.PRIMARY)
                .setSize(Size.LG)
                .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC-Bot(Help)", false)))
                .addModule(new SectionModule(new PlainTextElement("/ServerInfo - 反馈服务器当前信息"), null, null))
                .addModule(new SectionModule(new PlainTextElement("/ServerInfo SelfPVP - 查询自己的PVP数据(需绑定游戏账户)"), null, null))
                .addModule(new SectionModule(new PlainTextElement("/ServerInfo bind {PlayerName} - 绑定自己的游戏账户\nTips: 请注意绑定时您需要在游戏内"), null, null))
                .addModule(new SectionModule(new PlainTextElement("/ServerInfo changeBind {originPlayerName} {newPlayerName} - 更换绑定(Operator)\nTips: 请注意更换绑定时被更换的玩家需要在游戏内"), null, null))
                .addModule(new SectionModule(new PlainTextElement("/ServerInfo Help - 机器人使用帮助"), null, null))
                .addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))))
                .build();

        if (message != null) {
            message.reply(helpInfoCard);
        }
    }

    private void handleBindCommand(User sender, String playerName, Message message) {
        String playerUUID = "00000000-0000-0000-0000-000000000000";
        JSONObject jsonDataPack = getMcServerDataPackAnalysis.getServerInfo();
        JSONArray jsonArrayList = JSONArray.parse(jsonDataPack.getJSONObject("onlinePlayerList").getString("playerList"));

        for (Object item : jsonArrayList) {
            JSONObject temp = JSONObject.parse(item.toString());
            if (Objects.equals(temp.getString("name"), playerName)) {
                playerUUID = temp.getString("id");
                break;
            }
        }

        if (!Objects.equals(playerUUID, "00000000-0000-0000-0000-000000000000")) {
            if (jsonFileOperate.IfPlayerIsNoBinding(sender.getId())) {
                if (jsonFileOperate.AddNewBinding(playerName, playerUUID, sender.getId())) {
                    MultipleCardComponent bindCard = new CardBuilder()
                            .setTheme(Theme.PRIMARY)
                            .setSize(Size.LG)
                            .addModule(new HeaderModule(new PlainTextElement("DESOLATE-MC Server", false)))
                            .addModule(new SectionModule(new PlainTextElement("KOOK用户: " + sender.getName() + "\n游戏账户: " + playerName + "\n绑定成功!"), null, null))
                            .addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))))
                            .build();

                    if (message != null) {
                        message.reply(bindCard);
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
    }

    private void handleSelfPVPCommand(User sender, Message message) {
        if (!jsonFileOperate.IfPlayerIsNoBinding(sender.getId())) {
            JSONObject currentPlayerInfo = jsonFileOperate.getCurrentClassPlayerInfo();
            String cPlayerName = analysisYmlFile.getYmlValueFromYmlFile(currentPlayerInfo.getString("playerUUID") + ".name");
            String cPlayerKills = analysisYmlFile.getYmlValueFromYmlFile(currentPlayerInfo.getString("playerUUID") + ".kills");
            String cPlayerDeaths = analysisYmlFile.getYmlValueFromYmlFile(currentPlayerInfo.getString("playerUUID") + ".deaths");
            String cPlayerStreak = analysisYmlFile.getYmlValueFromYmlFile(currentPlayerInfo.getString("playerUUID") + ".streak");
            double kill = Double.parseDouble(cPlayerKills);
            double death = Double.parseDouble(cPlayerDeaths);
            double ratio = death == 0 ? kill : kill / death;
            String playerRatio = String.format("%.2f", ratio);

            MultipleCardComponent playerInfoCard = new CardBuilder()
                    .setTheme(Theme.PRIMARY)
                    .setSize(Size.LG)
                    .addModule(new HeaderModule(new PlainTextElement("DESOLATE-PVP", false)))
                    .addModule(new SectionModule(new PlainTextElement("游戏昵称: " + cPlayerName, false), null, null))
                    .addModule(new SectionModule(new PlainTextElement("击杀: " + cPlayerKills, false), null, null))
                    .addModule(new SectionModule(new PlainTextElement("死亡: " + cPlayerDeaths, false), null, null))
                    .addModule(new SectionModule(new PlainTextElement("K/D: " + playerRatio, false), null, null))
                    .addModule(new SectionModule(new PlainTextElement("最高连杀: " + cPlayerStreak, false), null, null))
                    .addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))))
                    .build();

            if (message != null) {
                message.reply(playerInfoCard);
            }
        } else {
            if (message != null) {
                message.reply("检测到你没有绑定游戏账户哦，快快去绑定一个叭！");
            }
        }
    }

    private void handleChangeBindCommand(User sender, String newPlayerName, String playerName, Message message) {
        if (jsonFileOperate.IfPlayerHavePermission(sender.getId())) {
            String playerUUID = "00000000-0000-0000-0000-000000000000";
            JSONObject jsonDataPack = getMcServerDataPackAnalysis.getServerInfo();
            JSONArray jsonArrayList = JSONArray.parse(jsonDataPack.getJSONObject("onlinePlayerList").getString("playerList"));

            for (Object item : jsonArrayList) {
                JSONObject temp = JSONObject.parse(item.toString());
                if (Objects.equals(temp.getString("name"), playerName)) {
                    playerUUID = temp.getString("id");
                    break;
                }
            }
            if (!Objects.equals(playerUUID, "00000000-0000-0000-0000-000000000000")) {
                if (jsonFileOperate.ChangePlayerBinding(newPlayerName, playerName, playerUUID)) {
                    if (message != null) {
                        message.reply("换绑成功！");
                    }
                } else {
                    if (message != null) {
                        message.reply("未检测到" + newPlayerName + "的绑定信息");
                    }
                }
            } else {
                if (message != null) {
                    message.reply("此玩家当前不在线！");
                }
            }
        } else {
            if (message != null) {
                message.reply("您没有管理权限，请联系管理员为您换绑！");
            }
        }
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

    private void handleCxCommand(User sender, Object[] args, Message message) {
        List<Object> arguments = Arrays.asList(args);
        String command = (String) arguments.get(0);

        switch (command.toLowerCase()) {
            case "default":
                // 检查是否绑定
                if (jsonFileOperate.IfPlayerIsNoBinding(sender.getId())) {
                    MultipleCardComponent multipleCardComponent = new CardBuilder()
                            .setTheme(Theme.PRIMARY)
                            .setSize(Size.LG)
                            .addModule(new HeaderModule(new PlainTextElement("DESOLATE-BOT", false)))
                            .addModule(new SectionModule(new PlainTextElement("未查询到绑定信息")))
                            .addModule(new SectionModule(new PlainTextElement("请先绑定账号或使用指令")))
                            .addModule(new SectionModule(new PlainTextElement("/cx 玩家ID")))
                            .addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))))
                            .build();
                    if (message != null) {
                        message.reply(multipleCardComponent);
                    }
                } else {
                    // 初始化第一页并发送
                    int initialPage = 0;
                    MultipleCardComponent component = buildCardForPage(initialPage);
                    if (message != null) {
                        message.reply(component);
                    }
                }
                break;
        }
    }

    public MultipleCardComponent buildCardForPage(int page) {
        JSONObject playerInfo = jsonFileOperate.getCurrentClassPlayerInfo();
        String playerName = playerInfo.getString("playerName");
        Set<String> keys = analysisYmlFile.getKeysForPlayer(playerName);

        // 计算当前页的开始和结束索引
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, keys.size());

        // 创建 CardBuilder 实例
        CardBuilder cardBuilder = new CardBuilder()
                .setTheme(Theme.PRIMARY)
                .setSize(Size.LG)
                .addModule(new HeaderModule(new PlainTextElement("DESOLATE-BOT | 武器击杀查询", false)))
                .addModule(new SectionModule(new PlainTextElement("玩家ID：" + playerName)));

        // 添加当前页的数据
        int index = 0;
        for (String key : keys) {
            if (index >= start && index < end) {
                Object value = analysisYmlFile.getPlayerKeyValue(playerName, key);
                cardBuilder.addModule(new SectionModule(new PlainTextElement(key + ": " + value)));
            }
            index++;
        }

        // 添加分页按钮
        List<InteractElement> interactElements = new ArrayList<>();
        if (page > 0) {
            interactElements.add(new ButtonElement(Theme.PRIMARY, "previous", ButtonElement.EventType.RETURN_VAL, new PlainTextElement("上一页")));
        }
        if (end < keys.size()) {
            interactElements.add(new ButtonElement(Theme.PRIMARY, "next", ButtonElement.EventType.RETURN_VAL, new PlainTextElement("下一页")));
        }

        // 只有在 interactElements 不为空时才添加 ActionGroupModule
        if (!interactElements.isEmpty()) {
            cardBuilder.addModule(new ActionGroupModule(interactElements));
        }
        cardBuilder.addModule(new ContextModule((List.of(new PlainTextElement("Designed by DESOLATE")))));
        // 构建 Component
        return cardBuilder.build();
    }

}