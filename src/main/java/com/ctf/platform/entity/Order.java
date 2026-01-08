package com.ctf.platform.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Integer id;
    private Integer userId;
    private BigDecimal amount;
    private String status; // PENDING, PAID
    private String tradeNo;
    private LocalDateTime createTime;
}
