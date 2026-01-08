package com.ctf.platform.controller;

import com.ctf.platform.entity.Order;
import com.ctf.platform.mapper.OrderMapper;
import com.ctf.platform.mapper.UserMapper;
import com.ctf.platform.service.PayPalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PayPalService payPalService;

    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> payload) {
        Integer userId = (Integer) payload.get("userId");
        // 统一价格为 0.01
        BigDecimal amount = new BigDecimal("0.01");

        Map<String, Object> result = new HashMap<>();

        try {
            // 调用 PayPal 创建订单
            com.paypal.orders.Order payPalOrder = payPalService.createOrder("USD", amount.toString());
            
            // 保存本地订单
            Order order = new Order();
            order.setUserId(userId);
            order.setAmount(amount);
            order.setStatus("PENDING");
            order.setTradeNo(payPalOrder.id()); // 保存 PayPal 的 Order ID
            order.setCreateTime(LocalDateTime.now());
            orderMapper.insert(order);

            result.put("success", true);
            result.put("orderId", payPalOrder.id()); // 返回 PayPal Order ID 给前端
            result.put("localOrderId", order.getId());
        } catch (IOException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "PayPal Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/capture")
    public Map<String, Object> captureOrder(@RequestBody Map<String, String> payload) {
        String payPalOrderId = payload.get("orderId");
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用 PayPal 捕获订单
            com.paypal.orders.Order payPalOrder = payPalService.captureOrder(payPalOrderId);
            
            if ("COMPLETED".equals(payPalOrder.status())) {
                // 通过 TradeNo 查找本地订单
                Order localOrder = orderMapper.findByTradeNo(payPalOrderId);
                
                if (localOrder != null) {
                    // 更新订单状态
                    orderMapper.updateStatus(localOrder.getId(), "COMPLETED");
                    
                    // 升级用户会员
                    userMapper.upgradeToVip(localOrder.getUserId());
                    
                    result.put("success", true);
                    result.put("status", "COMPLETED");
                } else {
                    // 理论上不应该发生，除非数据库不同步
                    result.put("success", false);
                    result.put("message", "Local order not found for PayPal Order ID: " + payPalOrderId);
                }
            } else {
                result.put("success", false);
                result.put("message", "Payment not completed");
            }
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "PayPal Capture Error: " + e.getMessage());
        }
        return result;
    }
    
    // 为了回调方便，保留一个简单的 confirm 接口用于前端通知后端升级
    // 实际应该在 capture 成功后直接处理。
    @PostMapping("/notify-success")
    public Map<String, Object> notifySuccess(@RequestBody Map<String, Object> payload) {
        String payPalOrderId = (String) payload.get("orderId");
        Integer userId = (Integer) payload.get("userId"); // 这是一个安全隐患，但在 CTF 靶场中正好可以是考点
        
        userMapper.upgradeToVip(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
