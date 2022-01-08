package cn.itcast.dtx.txmsgdemo.bank1.service.impl;

import cn.itcast.dtx.txmsgdemo.bank1.dao.AccountInfoDao;
import cn.itcast.dtx.txmsgdemo.bank1.model.AccountChangeEvent;
import cn.itcast.dtx.txmsgdemo.bank1.service.AccountInfoService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService {

    @Autowired
    AccountInfoDao accountInfoDao;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    //向MQ发送消息
    @Override
    public void sendUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {
        //构造消息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accountChange", accountChangeEvent);
        String jsonString = jsonObject.toJSONString();
        Message<String> message = MessageBuilder.withPayload(jsonString).build();

        /**
         * String txProducerGroup  生产组
         * String destination      topic，发送到哪个主题
         * Message<?> message      消息内容.必须传这个参数
         * Object arg
         */
        //发送一条事务消息，事务消息时MQ自己提供的。在最大努力通知中就可以不用事务消息
        rocketMQTemplate.sendMessageInTransaction(
                "producer_group_txmsg_bank1",
                "topic_txmsg", message, null);
    }

    //更新账户，要实现幂等。避免重复扣减
    //producer发送消息完成后，会执行本地的回调方法executeLocalTransaction(Message message, Object o)。
    //在回调方法中会执行本地事务，也就是该方法。
    @Override
    @Transactional
    public void doUpdateAccountBalance(AccountChangeEvent accountChangeEvent) {

        log.info("开始更新本地事务，事务号：{}", accountChangeEvent.getTxNo());
        //幂等判断
        if (accountInfoDao.isExistTx(accountChangeEvent.getTxNo()) > 0) {
            return;
        }
        //扣减金额
        accountInfoDao.updateAccountBalance(accountChangeEvent.getAccountNo(), accountChangeEvent.getAmount() * -1);
        //添加事务日志
        accountInfoDao.addTx(accountChangeEvent.getTxNo());

        log.info("结束更新本地事务，事务号：{}", accountChangeEvent.getTxNo());
    }
}
