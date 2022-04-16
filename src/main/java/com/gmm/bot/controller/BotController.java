package com.gmm.bot.controller;

import com.gmm.bot.run.BotSchedule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;

@Controller
public class BotController implements InitializingBean {

    private final BotSchedule botSchedule;

    public BotController(BotSchedule botSchedule) {
        this.botSchedule = botSchedule;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("START BOT");
        botSchedule.createOneBot();
    }
}
