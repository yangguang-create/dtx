package cn.itcast.dtx.tccdemo.bank2.service.impl;

import cn.itcast.dtx.tccdemo.bank2.dao.AccountInfoDao;
import cn.itcast.dtx.tccdemo.bank2.service.AccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccountInfoServiceImpl implements AccountInfoService {


    @Autowired
    AccountInfoDao accountInfoDao;



    //账户扣款，就是tcc的try方法，就是全局事务的入口
    @Override
    @Hmily(confirmMethod = "commit",cancelMethod = "rollback")
    //只要标记这个注解的方法，就是try方法。在注解中指定confirm和 cancel
    //也就是说 Hmily只要求暴露try方法，并且指定confirm 和 cancel方法
    //而且，confirm和cancel方法的三个参数是一样的。
    @Transactional
    public void updateAccountBalance(String accountNo, Double amount) {
        //hmily获取全局事务编号
        String transId = HmilyTransactionContextLocal.getInstance().get().getTransId();
        log.info("******* Bank2 Service begin try...." + transId);
    }

    //confirm 方法

    /**
     * confirm幂等校验
     * 正式增加金额
     * 添加confirm日志，方便下次幂等校验
     * @param accountNo
     * @param amount
     */
    @Transactional
    public void commit(String accountNo, Double amount) {
        String localTradeNo = HmilyTransactionContextLocal.getInstance().get().getTransId();
        log.info("******** Bank2 Service begin commit..."+localTradeNo );
        //confirm 幂等校验
        if (accountInfoDao.isExistConfirm(localTradeNo) > 0) {//幂等性校验，已经执行过了，什么也不用做
            log.info("******** Bank2 已经执行过confirm... 无需再次confirm " + localTradeNo);
            return;
        }
        //增加金额
        accountInfoDao.addAccountBalance(accountNo, amount);
        //添加confirm日志。方便下次做幂等校验
        accountInfoDao.addConfirm(localTradeNo);
    }

    //cancel方法
    @Transactional
    public void rollback(String accountNo, Double amount) {
        String transId = HmilyTransactionContextLocal.getInstance().get().getTransId();
        log.info("******** Bank2 Service begin commit..."+transId );
    }
}
