package cn.itcast.dtx.tccdemo.bank1.spring;

import cn.itcast.dtx.tccdemo.bank1.spring.impl.Bank2ClientFallback;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "tcc-demo-bank2", fallback = Bank2ClientFallback.class)
public interface Bank2Client {

    //远程调用李四的微服务
    @GetMapping("/bank2/transfer")
    @Hmily//需要在远程调用的时候。通过这个注解把全局事务信息带给李四。
    Boolean transfer(@RequestParam("amount") Double amount);


}
