package cn.itcast.dtx.notifydemo.bank1.spring.impl;

import cn.itcast.dtx.notifydemo.bank1.entity.AccountPay;
import cn.itcast.dtx.notifydemo.bank1.spring.PayClient;
import org.springframework.stereotype.Component;

@Component
public class PayFallBackImpl implements PayClient {


    @Override
    public AccountPay queryPayResult(String txNo) {
        AccountPay accountPay = new AccountPay();
        accountPay.setResult("fail");
        return accountPay;
    }

}
