package cn.itcast.dtx.notifydemo.bank1.controller;

import cn.itcast.dtx.notifydemo.bank1.entity.AccountPay;
import cn.itcast.dtx.notifydemo.bank1.service.AccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AccountInfoController {

    @Autowired
    AccountInfoService accountInfoService;

    @RequestMapping(value = "/payresult/{txNo}")
    public AccountPay result(@PathVariable("txNo") String txNo) {
        AccountPay accountPay = accountInfoService.queryPayResult(txNo);
        return accountPay;
    }
}
