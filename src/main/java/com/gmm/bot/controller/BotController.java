package com.gmm.bot.controller;

import com.gmm.bot.base.BaseBot;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class BotController implements InitializingBean {
    @Autowired
    private ObjectFactory<BaseBot> botObjectFactory;

    @Override
    public void afterPropertiesSet() {
        BaseBot bot =  botObjectFactory.getObject();
        bot.connectSmartFox();
        System.out.println("Start bot "+ bot.getUsername()+" successfully");
    }
}
