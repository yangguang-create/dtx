package cn.itcast.dtx.txmsgdemo.bank1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
//封装中间类，注意这里不是直接映射到数据时的类
public class AccountChangeEvent implements Serializable {
    /**
     * 账号
     */
    private String accountNo;
    /**
     * 变动金额
     */
    private double amount;
    /**
     * 事务号：用来保证幂等性
     */
    private String txNo;

}
