package com.gmm.bot.run;

import com.gmm.bot.base.BotBase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class BotSchedule {
    @Autowired
    private ObjectFactory<BotBase> botObjectFactory;

    public String createOneBot(){
        BotBase bot =  botObjectFactory.getObject();
        bot.connectSmartfox();
        return "Start bot "+ bot.getUsername()+" successfully";
    }

    private void log(String message){
        log.info(message);
    }
}
