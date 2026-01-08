package com.ctf.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 预留第三方登录接口
 * 实际生产环境需集成 Spring Security OAuth2 或相关 SDK
 */
@Controller
public class OAuthController {

    @GetMapping("/auth/callback")
    @ResponseBody
    public Map<String, Object> oauthCallback(@RequestParam String provider, @RequestParam String code) {
        // 模拟 OAuth 流程
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("provider", provider);
        response.put("user", "OAuth_User_" + System.currentTimeMillis());
        response.put("message", "Authentication successful via " + provider);
        return response;
    }

    @GetMapping("/auth/login")
    public String redirectToProvider(@RequestParam String provider) {
        // 模拟重定向到第三方平台
        // return "redirect:https://github.com/login/oauth/authorize?client_id=YOUR_ID";
        return "redirect:/";
    }
}
