package com.gmm.bot.controller;

import com.gmm.bot.ai.GemBot;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class BotController implements InitializingBean {
    @Autowired
    private ObjectFactory<GemBot> botObjectFactory;

    @Override
    public void afterPropertiesSet() {
        GemBot bot =  botObjectFactory.getObject();
        bot.start();
        System.out.println("Start bot "+ bot.getUsername()+" successfully");
    }
}
