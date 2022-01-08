package cn.itcast.dtx.notifymsg.pay.service.impl;

import cn.itcast.dtx.notifymsg.pay.dao.AccountPayDao;
import cn.itcast.dtx.notifymsg.pay.entity.AccountPay;
import cn.itcast.dtx.notifymsg.pay.service.AccountPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountPayServiceImpl implements AccountPayService{

    @Autowired
    AccountPayDao accountPayDao;

    @Autowired
    RocketMQTemplate rocketMQTemplate;

    /**
     * 1:充值
     * 2：充值之后，要通知，也就是发送给MQ
     * @param accountPay
     * @return
     */
    @Override
    @Transactional
    public AccountPay insertAccountPay(AccountPay accountPay) {
        int success = accountPayDao.insertAccountPay(accountPay.getId(),
                accountPay.getAccountNo(),
                accountPay.getPayAmount(),
                "success");
        //充值成功后，进行通知
        if (success > 0) {
            //这里使用的是普通消息发送通知。和可靠消息一致性方案不一样
            accountPay.setResult("success");//发送消息的时候，要把充值结果带上。
            rocketMQTemplate.convertAndSend("topic_notifymsg", accountPay);
            return accountPay;//直接返回消息
        }
        return null;
    }

    /**
     * 查询插入记录，接收方调用此方法查询充值结果
     * @param txNo
     * @return
     */
    @Override
    public AccountPay getAccountPay(String txNo) {
        AccountPay accountPay = accountPayDao.findByIdTxNo(txNo);
        return accountPay;
    }
}
