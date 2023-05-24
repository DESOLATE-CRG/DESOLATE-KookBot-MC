package org.desolate;

import snw.jkook.plugin.BasePlugin;

public class KookBotMain extends BasePlugin{
    private static KookBotMain instance;

    @Override
    public void onLoad() {
        instance = this;
        MyLogger("插件正在加载...");
    }

    @Override
    public void onEnable() {
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