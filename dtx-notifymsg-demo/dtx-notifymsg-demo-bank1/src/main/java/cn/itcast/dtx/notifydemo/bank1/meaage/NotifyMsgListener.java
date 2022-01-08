package cn.itcast.dtx.notifydemo.bank1.meaage;

import cn.itcast.dtx.notifydemo.bank1.entity.AccountPay;
import cn.itcast.dtx.notifydemo.bank1.model.AccountChangeEvent;
import cn.itcast.dtx.notifydemo.bank1.service.AccountInfoService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "topic_notifymsg",consumerGroup = "consumer_group_notifymsg_bank1")
public class NotifyMsgListener implements RocketMQListener<AccountPay> {

    @Autowired
    AccountInfoService accountInfoService;

    /**
     * 接收MQ发送过来的消息
     * @param message
     */
    @Override
    public void onMessage(AccountPay message) {
        log.info("接收到消息：{}", JSON.toJSONString(message));
        //接收消息，更新账户
        if ("success".equals(message.getResult())) {
            AccountChangeEvent accountChangeEvent = new AccountChangeEvent();
            accountChangeEvent.setAccountNo(message.getAccountNo());
            accountChangeEvent.setAmount(message.getPayAmount());
            accountChangeEvent.setTxNo(message.getId());
            //主动更新本地账户
            accountInfoService.updateAccountBalance(accountChangeEvent);
        }
        log.info("处理消息完成：{}", JSON.toJSONString(message));
    }
}
