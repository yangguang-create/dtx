package cn.itcast.dtx.notifydemo.bank1.spring;

import cn.itcast.dtx.notifydemo.bank1.entity.AccountPay;
import cn.itcast.dtx.notifydemo.bank1.spring.impl.PayFallBackImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "dtx-notifymsg-demo-pay", fallback = PayFallBackImpl.class)
public interface PayClient {

    @GetMapping("/pay/result/{txNo}")
    AccountPay queryPayResult(@PathVariable("txNo") String txNo);
}
