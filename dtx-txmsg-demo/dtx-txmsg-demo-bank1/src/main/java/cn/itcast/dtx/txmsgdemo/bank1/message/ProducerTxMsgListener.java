package cn.itcast.dtx.txmsgdemo.bank1.message;

import cn.itcast.dtx.txmsgdemo.bank1.dao.AccountInfoDao;
import cn.itcast.dtx.txmsgdemo.bank1.model.AccountChangeEvent;
import cn.itcast.dtx.txmsgdemo.bank1.service.AccountInfoService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springfox.documentation.spring.web.json.Json;

@Component
@Slf4j
@RocketMQTransactionListener(txProducerGroup = "producer_group_txmsg_bank1")//和发送消息的时候填写的一致
public class ProducerTxMsgListener implements RocketMQLocalTransactionListener {

    @Autowired
    AccountInfoService accountInfoService;

    @Autowired
    AccountInfoDao accountInfoDao;

    /**
     * 事务消息发送成功后,MQ会自动回调此方法。
     * 回调后，需要执行本地事务
     *
     * @param message   : MQ回调时， 发送过来的信息。
     *                   其实和发送给MQ的信息是一样的。
     * @param o      调用send方法时传递的参数，
     *               当send时候若有额外的参数可以传递到send方法中，这里能获取到
     * @return  返回的是本地事务的执行状态。有三种枚举类型：
     *          Commit : 提交，此时MQ就可以把消息投递给消费者。MQ将消息的状态改成可消费
     *          ROLLBACK : 回滚，此时MQ就会把消息丢掉。不会再发送给消费者
     *          UNKNOW : 回调
     */
    @Override
    @Transactional
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            //解析message。然后转成AccountChangeEvent
            String messageString = new String((byte[]) message.getPayload());
            final JSONObject jsonObject = JSONObject.parseObject(messageString);
//            String accountChange = (String) jsonObject.get("accountChange");
            //将json格式的accountChange转成AccountChangeEvent对象
            AccountChangeEvent accountChangeEvent = JSONObject.parseObject(jsonObject.getString("accountChange"), AccountChangeEvent.class);
            //执行本地事务。
            accountInfoService.doUpdateAccountBalance(accountChangeEvent);
            //正常执行时，返回本地事务的状态。
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("executeLocalTransaction 事务执行失败",e);
            e.printStackTrace();
            //如果上面的本地事务执行时出现异常，就会给rollBack。这是MQ就会把消息丢失掉。
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 事务状态的回查，也就是检查本地服务是否扣减金额。
     * 检查本地事务的执行状态
     * @param message   获取本地事务id，判断本地事务是不是执行成功了。
     * @return          返回本地事务状态，COMMIT:提交
     *                                  ROLLBACK:回滚
     *                                  UNKNOWN: 回调，表示状态不一定，可以继续查询
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        RocketMQLocalTransactionState state;
        //解析message。然后转成AccountChangeEvent
        String messageString = new String((byte[]) message.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String accountChange = (String) jsonObject.get("accountChange");
        //将json格式的accountChange转成AccountChangeEvent对象
        AccountChangeEvent accountChangeEvent = JSONObject.parseObject(accountChange, AccountChangeEvent.class);

        //这时可以拿到事务id.
        String txNo = accountChangeEvent.getTxNo();
        //查询事物的日志是否写进来
        int existTx = accountInfoDao.isExistTx(txNo);
        log.info("回查事务，事务号: {} 结果: {}", accountChangeEvent.getTxNo(),existTx);
        if (existTx > 0) {
            state = RocketMQLocalTransactionState.COMMIT;
        } else {
            state = RocketMQLocalTransactionState.UNKNOWN;
        }
        return state;
    }
}
