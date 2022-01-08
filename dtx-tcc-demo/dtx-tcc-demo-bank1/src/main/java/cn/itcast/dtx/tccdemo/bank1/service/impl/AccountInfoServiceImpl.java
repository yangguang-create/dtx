package cn.itcast.dtx.tccdemo.bank1.service.impl;

import cn.itcast.dtx.tccdemo.bank1.dao.AccountInfoDao;
import cn.itcast.dtx.tccdemo.bank1.service.AccountInfoService;
import cn.itcast.dtx.tccdemo.bank1.spring.Bank2Client;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.dromara.hmily.common.bean.context.HmilyTransactionContext;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.dromara.hmily.core.concurrent.threadlocal.HmilyTransactionContextLocal;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.logging.Logger;

@Slf4j
@Service
public class AccountInfoServiceImpl implements AccountInfoService {


    @Autowired
    AccountInfoDao accountInfoDao;

    @Autowired
    Bank2Client bank2Client;


    /**
     * try幂等校验
     * try悬挂处理
     * 检查余额是够扣减金额
     * 扣减金额
     * @param accountNo
     * @param amount
     */
    //账户扣款，就是tcc的try方法，就是全局事务的入口
    @Override
    @Hmily(confirmMethod = "commit",cancelMethod = "rollback")
    //只要标记这个注解的方法，就是try方法。在注解中指定confirm和 cancle 。
    //也就是说hmily只要求暴露try方法，并且指定confirm 和 cancle方法
    //而且，confirm和cancle方法的三个参数是一样的。
    @Transactional
    public void updateAccountBalance(String accountNo, Double amount) {
        //hmily获取全局事务编号
        String transId = HmilyTransactionContextLocal.getInstance().get().getTransId();
        log.info("******* Bank1 Service begin try...." + transId);
        //try幂等校验
        int existTry = accountInfoDao.isExistTry(transId);
        if (existTry > 0) {
            log.info("******* Bank1 Service 已经执行了try,无需重复执行 ，事务id : {}", transId);
            return;
        }
        //try 悬挂处理.(悬挂指的是cancle或者confirm执行了，但是try没有执行)
        if (accountInfoDao.isExistCancel(transId) > 0 || accountInfoDao.isExistConfirm(transId) > 0) {
            log.info("******* Bank1 Service 已经执行了confirm 或 cancle, 悬挂处理 ,事务id : {}", transId);
            return;
        }
        //扣钱
        if (accountInfoDao.subtractAccountBalance(accountNo, amount) <= 0) {
            throw new RuntimeException("bank1 exception，扣减失败，事务id:{}" + transId);
        }

        //增加本地的try成功记录，用于幂等性的控制标识
        accountInfoDao.addTry(transId);

        //远程调用李四的微服务
        if (!bank2Client.transfer(amount)) {
            throw new HmilyRuntimeException("bank2Client exception，事务id:{}"+ transId);
        }

        if (amount == 2) {
            throw new RuntimeException("认为制造异常，xid {}" + transId);
        }
    }

    //confirm 方法
    public void commit(String accountNo, Double amount) {
        String localTradeNo = HmilyTransactionContextLocal.getInstance().get().getTransId();
        log.info("******** Bank1 Service begin commit..."+localTradeNo );
    }

    //cancle方法
    /**
     * cancel幂等校验
     * cancel空回滚处理
     * 增加可用余额
     * 添加cancel日志
     * @param accountNo
     * @param amount
     */
    @Transactional
    public void rollback(String accountNo, Double amount) {
        String transId = HmilyTransactionContextLocal.getInstance().get().getTransId();
        //cancel幂等校验
        if (accountInfoDao.isExistCancel(transId) > 0) {
            log.info("******** Bank1 已经执行过rollback... 无需再次rollback " +transId);
            return;
        }
        //cancel空回滚处理.如果try没有执行，cancel不允许执行（空回滚指的是，try没有执行，cancel执行了）
        if (accountInfoDao.isExistTry(transId) <= 0) {
            log.info("******** Bank1 try阶段失败... 无需rollback " + transId);
            return;
        }
        //增加账户金额
        accountInfoDao.addAccountBalance(accountNo, amount);
        //添加cancel日志，用于幂等性的控制标识
        accountInfoDao.addCancel(transId);
        log.info("******** Bank1 Service end rollback... " + transId);
    }
}
