package cn.itcast.dtx.txmsgdemo.bank1.service;

import cn.itcast.dtx.txmsgdemo.bank1.model.AccountChangeEvent;

public interface AccountInfoService {

    //向MQ发送消息
    public void sendUpdateAccountBalance(AccountChangeEvent accountChangeEvent);

    //更新账户
    public void doUpdateAccountBalance(AccountChangeEvent accountChangeEvent);
}
