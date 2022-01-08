package cn.itcast.dtx.txmsgdemo.bank2.service.impl;

import cn.itcast.dtx.txmsgdemo.bank2.dao.AccountInfoDao;
import cn.itcast.dtx.txmsgdemo.bank2.model.AccountChangeEvent;
import cn.itcast.dtx.txmsgdemo.bank2.service.AccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AccountInfoServiceImpl implements AccountInfoService {

    @Autowired
    AccountInfoDao accountInfoDao;


    /**
     * 更新账户,为了避免消息重复消费，这里需要实现幂等。
     * @param accountChangeEvent
     */
    @Override
    @Transactional
    public void addAccountInfoBalance(AccountChangeEvent accountChangeEvent) {
        log.info("bank2更新本地账号，账号：{},金额： {}"
                , accountChangeEvent.getAccountNo()
                , accountChangeEvent.getAmount());
        //幂等校验
        int existTx = accountInfoDao.isExistTx(accountChangeEvent.getTxNo());
        if (existTx <= 0) {
            //消息还没有被消费，那就可以更细账户
            accountInfoDao.updateAccountBalance(accountChangeEvent.getAccountNo(), accountChangeEvent.getAmount());
            //添加事务记录，方便下次幂等校验
            accountInfoDao.addTx(accountChangeEvent.getTxNo());
            log.info("更新本地事务执行成功，本次事务号: {}", accountChangeEvent.getTxNo());
        } else {
            log.info("更新本地事务执行失败，本次事务号: {}", accountChangeEvent.getTxNo());
        }

    }
}
