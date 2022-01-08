package cn.itcast.dtx.txmsgdemo.bank2.mesage;

import cn.itcast.dtx.txmsgdemo.bank2.dao.AccountInfoDao;
import cn.itcast.dtx.txmsgdemo.bank2.model.AccountChangeEvent;
import cn.itcast.dtx.txmsgdemo.bank2.service.AccountInfoService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
/**
 *  consumerGroup：消费组的意思，就相当于本服务在MQ中的服务名
 *                 MQ重复发送消息的时候，是根据这个服务名来发送的
 */
@RocketMQMessageListener(consumerGroup = "consumer_group_txmsg_bank2",topic = "topic_txmsg")
public class TxmsgConsumer implements RocketMQListener<String> {

    @Autowired
    AccountInfoService accountInfoService;

    //接收消息
    @Override
    public void onMessage(String message) {
        log.info("开始消费消息:{}", message);
        //接收消息之后需要解析消息
        JSONObject jsonObject = JSONObject.parseObject(message);
        AccountChangeEvent accountChange = JSONObject.parseObject(jsonObject.getString("accountChange"), AccountChangeEvent.class);
        //原来消息的账号是张三的，现在要设置成李四的。
        accountChange.setAccountNo("2");
        accountInfoService.addAccountInfoBalance(accountChange);
    }

}
