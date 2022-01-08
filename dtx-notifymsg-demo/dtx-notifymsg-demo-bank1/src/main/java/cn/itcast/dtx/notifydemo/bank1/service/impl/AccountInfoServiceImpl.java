package cn.itcast.dtx.notifydemo.bank1.service.impl;

import cn.itcast.dtx.notifydemo.bank1.dao.AccountInfoDao;
import cn.itcast.dtx.notifydemo.bank1.entity.AccountPay;
import cn.itcast.dtx.notifydemo.bank1.model.AccountChangeEvent;
import cn.itcast.dtx.notifydemo.bank1.service.AccountInfoService;
import cn.itcast.dtx.notifydemo.bank1.spring.PayClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService {

    @Autowired
    AccountInfoDao accountInfoDao;

    @Autowired
    PayClient payClient;


    /**
     *  由于用户服务是在监听MQ的，所以MQ会回调这方法。
     *  更新账户金额
     *  由于MQ会重复的向用户微服务发送消息，所以可能会重复的更新用户金额，
     *  因此需要实现幂等控制
     */
    @Override
    @Transactional
    public void updateAccountBalance(AccountChangeEvent accountChange) {
        //幂等校验
        if (accountInfoDao.isExistTx(accountChange.getTxNo()) > 0) {
            log.info("已处理消息：{}", JSONObject.toJSONString(accountChange));
            return;
        }
        //更改金额
        int i = accountInfoDao.updateAccountBalance(accountChange.getAccountNo(), accountChange.getAmount());
        //插入事务记录，用于幂等控制
        accountInfoDao.addTx(accountChange.getTxNo());
    }

    /**
     * 远程调用查询充值结果
     *
     * @param tx_No
     * @return
     */
    @Override
    public AccountPay queryPayResult(String tx_No) {
        AccountPay accountPay = payClient.queryPayResult(tx_No);
        //充值结果
        String result = accountPay.getResult();
        log.info("主动查询充值结果：{}", JSON.toJSONString(accountPay));
        if ("success".equals(result)) {
            AccountChangeEvent accountChangeEvent = new AccountChangeEvent();
            accountChangeEvent.setAccountNo(accountPay.getAccountNo());
            accountChangeEvent.setAmount(accountPay.getPayAmount());
            accountChangeEvent.setTxNo(accountPay.getId());
            //主动更新本地账户
            updateAccountBalance(accountChangeEvent);
        }
        return accountPay;
    }
}
