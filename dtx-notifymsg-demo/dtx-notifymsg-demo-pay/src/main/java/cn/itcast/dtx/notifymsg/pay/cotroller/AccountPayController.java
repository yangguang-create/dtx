package cn.itcast.dtx.notifymsg.pay.cotroller;

import cn.itcast.dtx.notifymsg.pay.entity.AccountPay;
import cn.itcast.dtx.notifymsg.pay.service.AccountPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AccountPayController {

    @Autowired
    AccountPayService accountPayService;

    /**
     * 更改金额
     * @param accountPay
     * @return
     */
    @RequestMapping(value = "/paydo")
    public AccountPay pay(AccountPay accountPay) {
        //事务号
        String txID = UUID.randomUUID().toString();
        accountPay.setId(txID);
        return accountPayService.insertAccountPay(accountPay);
    }

    /**
     * 查询充值结果
     * @param txNo
     * @return
     */
    @GetMapping(value = "/pay/result/{txNo}")
    public AccountPay payResult(@PathVariable("txNo") String txNo) {
        return accountPayService.getAccountPay(txNo);
    }

}
