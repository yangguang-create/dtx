package cn.itcast.dtx.seatademo.bank1.service.impl;

import cn.itcast.dtx.seatademo.bank1.dao.AccountInfoDao;
import cn.itcast.dtx.seatademo.bank1.service.AccountInfoService;
import cn.itcast.dtx.seatademo.bank1.spring.Bank2Client;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
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
    Bank2Client bank2Client;

    @GlobalTransactional//控制全局事务:也就是全局事务的发起方。
    @Transactional//控制本地事务
    @Override
    public void updateAccountBalance(String accountNo, Double amount) {
        log.info("bank1 service begin, XID:{}" + RootContext.getXID());
        //扣减张三的金额
        accountInfoDao.updateAccountBalance(accountNo, amount * -1);
        //调用李四的微服务 转账
        String fallback = bank2Client.transfer(amount);
        if ("fallback".equals(fallback)) {
            //调用李四微服务异常
            //出现异常我们需要抛出，只要一抛出异常。Seata这个代理就能感知到。
            // 说明分支事务执行失败。
            throw new RuntimeException("调用李四微服务异常。");
        }
        if (amount == 2) {
            throw new RuntimeException(" bank1 make exception...");
        }


    }
}
