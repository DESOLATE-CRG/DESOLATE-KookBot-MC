package org.desolate;

import snw.jkook.entity.User;
import snw.jkook.event.EventHandler;
import snw.jkook.event.Listener;
import snw.jkook.event.user.UserClickButtonEvent;
import snw.jkook.message.component.card.MultipleCardComponent;

public class EventListener implements Listener {

    @EventHandler
    public void onUserClickButton(UserClickButtonEvent event) {
        String buttonValue = event.getValue();
        User user = event.getUser();

        // 获取用户当前的页数，默认值为 0
        int currentPage = KookBotMain.getInstance().userPages.getOrDefault(user, 0);

        // 更新页数
        if (buttonValue.equals("next")) {
            currentPage++;
        } else if (buttonValue.equals("previous")) {
            currentPage--;
        }

        // 确保页数在有效范围内
        currentPage = Math.max(currentPage, 0);
        KookBotMain.getInstance().userPages.put(user, currentPage);

        // 生成并发送更新后的消息
        MultipleCardComponent component = KookBotMain.getInstance().buildCardForPage(currentPage);
        user.sendPrivateMessage(component);
    }

}
