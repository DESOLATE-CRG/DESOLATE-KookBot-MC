package org.desolate;

import snw.jkook.entity.Guild;
import snw.jkook.event.EventHandler;
import snw.jkook.event.Listener;
import snw.jkook.event.channel.ChannelMessageEvent;

public class MyEventListener implements Listener {
    @EventHandler
    public void allChannelMessageEvent(ChannelMessageEvent event) {
        Guild guild = event.getChannel().getGuild();
        KookBotMain.setCurrentMessageGuild(guild);
    }
}