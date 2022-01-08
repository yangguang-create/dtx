package cn.itcast.dtx.tccdemo.bank1.controller;

import cn.itcast.dtx.tccdemo.bank1.service.AccountInfoService;
import cn.itcast.dtx.tccdemo.bank1.service.impl.AccountInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Bank1Controller {
    @Autowired
    AccountInfoService accountInfoServiceImpl;

    @RequestMapping("/transfer")
    public String test(@RequestParam("amount") Double amount) {
        accountInfoServiceImpl.updateAccountBalance("1", amount);
        return "cn/itcast/dtx/tccdemo/bank1" + amount;
    }
}
