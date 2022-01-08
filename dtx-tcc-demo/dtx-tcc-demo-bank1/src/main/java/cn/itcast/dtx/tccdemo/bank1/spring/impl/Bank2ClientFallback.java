package cn.itcast.dtx.tccdemo.bank1.spring.impl;


import cn.itcast.dtx.tccdemo.bank1.spring.Bank2Client;
import org.springframework.stereotype.Component;

@Component
public class Bank2ClientFallback implements Bank2Client {
    @Override
    public Boolean transfer(Double amount) {
        return false;
    }

}
