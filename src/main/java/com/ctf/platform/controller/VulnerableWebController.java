package com.ctf.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class VulnerableWebController {

    private String getCommonStyle() {
        return "<head><meta charset='UTF-8'><title>Target System</title>" +
               "<link rel='stylesheet' href='/css/style.css'>" +
               "<style>" +
               "body { display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }" +
               ".hack-panel { width: 100%; max-width: 500px; padding: 30px; border: 2px solid #00ff00; box-shadow: 0 0 20px rgba(0,255,0,0.2); }" +
               "input { width: 100%; margin-bottom: 15px; }" +
               "</style></head>";
    }

    // --- SQL Injection Demo ---
    @GetMapping("/challenge/web/sqli")
    @ResponseBody
    public String sqliPage() {
        return "<html>" + getCommonStyle() + "<body>" +
               "<div class='panel hack-panel'>" +
               "<h2>安全登录系统</h2>" +
               "<p style='color:#888'>请输入您的凭证。</p>" +
               "<form id='loginForm' onsubmit='event.preventDefault(); doLogin()'>" +
               "<input type='text' id='u' placeholder='用户名' required>" +
               "<input type='password' id='p' placeholder='密码'>" +
               "<button type='submit' class='btn-full'>登录</button>" +
               "</form>" +
               "<div id='msg' class='message'></div>" +
               "<script>" +
               "async function doLogin() {" +
               "  const u = document.getElementById('u').value;" +
               "  const p = document.getElementById('p').value;" +
               "  const formData = new FormData(); formData.append('username', u); formData.append('password', p);" +
               "  const res = await fetch('/challenge/web/sqli/login', { method: 'POST', body: formData });" +
               "  const data = await res.json();" +
               "  const msg = document.getElementById('msg');" +
               "  msg.textContent = data.message;" +
               "  msg.className = 'message ' + (data.success ? 'success' : 'error');" +
               "}" +
               "</script>" +
               "</div></body></html>";
    }

    @PostMapping("/challenge/web/sqli/login")
    @ResponseBody
    public Map<String, Object> sqliLogin(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        if (username.contains("' OR 1=1") || username.contains("' OR '1'='1") || username.toLowerCase().contains("admin' --")) {
            result.put("success", true);
            result.put("message", "访问成功。Flag: ctf{sqli_master}");
        } else {
            result.put("success", false);
            result.put("message", "访问被拒绝。凭证无效。");
        }
        return result;
    }

    // --- Robots Protocol Demo ---
    @GetMapping("/robots.txt")
    @ResponseBody
    public String robots() {
        return "User-agent: *\nDisallow: /secret_admin_path_123";
    }

    @GetMapping("/secret_admin_path_123")
    @ResponseBody
    public String secretAdmin() {
        return "<html>" + getCommonStyle() + "<body><div class='panel hack-panel'>" +
               "<h1 style='color:red'>绝密</h1>" +
               "<p>恭喜！你发现了隐藏的管理员页面。</p>" +
               "<div class='message success'>Flag: ctf{robots_txt_is_public}</div>" +
               "</div></body></html>";
    }

    // --- Cookie Forgery Demo ---
    @GetMapping("/challenge/web/cookie")
    @ResponseBody
    public String cookieChallenge(@CookieValue(value = "is_admin", defaultValue = "false") String isAdmin) {
        String content;
        if ("true".equals(isAdmin)) {
            content = "<h2 style='color:#00ff00'>管理员仪表盘</h2>" +
                      "<p>欢迎回来，管理员。</p>" +
                      "<div class='message success'>Flag: ctf{yummy_admin_cookie}</div>";
        } else {
            content = "<h2>访客仪表盘</h2>" +
                      "<p>您当前以 <strong>访客</strong> 身份登录。</p>" +
                      "<p>只有管理员才能查看 Flag。</p>" +
                      "<div class='message error'>访问受限</div>" +
                      "<script>if(document.cookie.indexOf('is_admin')===-1) document.cookie='is_admin=false; path=/';</script>";
        }
        return "<html>" + getCommonStyle() + "<body><div class='panel hack-panel'>" + content + "</div></body></html>";
    }
}
