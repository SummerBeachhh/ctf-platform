package com.ctf.platform.controller;

import com.ctf.platform.entity.User;
import com.ctf.platform.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");

        User user = userMapper.findByUsername(username);
        Map<String, Object> result = new HashMap<>();

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            result.put("success", true);
            result.put("user", user);
        } else {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        
        Map<String, Object> result = new HashMap<>();

        if (userMapper.findByUsername(username) != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        userMapper.insert(user);

        result.put("success", true);
        result.put("message", "注册成功");
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            // Refresh user data from DB to get latest score
            User freshUser = userMapper.findById(user.getId());
            session.setAttribute("user", freshUser);
            result.put("success", true);
            result.put("user", freshUser);
        } else {
            result.put("success", false);
        }
        return result;
    }
}
