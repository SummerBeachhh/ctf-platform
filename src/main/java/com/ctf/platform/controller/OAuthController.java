package com.ctf.platform.controller;

import com.ctf.platform.entity.User;
import com.ctf.platform.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;

/**
 * 处理第三方登录接口
 * 实际生产环境需集成 Spring Security OAuth2 或相关 SDK
 */
@Controller
public class OAuthController {

    @Autowired
    private UserMapper userMapper;

    @org.springframework.beans.factory.annotation.Value("${github.client-id}")
    private String githubClientId;

    @org.springframework.beans.factory.annotation.Value("${github.client-secret}")
    private String githubClientSecret;
    
    @org.springframework.beans.factory.annotation.Value("${github.callback-url}")
    private String callbackUrl;

    @GetMapping("/auth/login")
    public String redirectToProvider(@RequestParam String provider) {
        if ("github".equalsIgnoreCase(provider)) {
            String githubAuthUrl = "https://github.com/login/oauth/authorize" +
                    "?client_id=" + githubClientId +
                    "&redirect_uri=" + callbackUrl +
                    "&scope=read:user";
            return "redirect:" + githubAuthUrl;
        }
        return "redirect:/";
    }

    @GetMapping("/auth/callback")
    public String oauthCallback(@RequestParam(required = false) String code, 
                                @RequestParam(required = false) String error,
                                HttpSession session) {
        if (error != null || code == null) {
            return "redirect:/?error=oauth_failed";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Exchange code for access token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            Map<String, String> tokenParams = Map.of(
                "client_id", githubClientId,
                "client_secret", githubClientSecret,
                "code", code,
                "redirect_uri", callbackUrl
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(tokenParams, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);
            
            if (tokenResponse.getBody() == null || !tokenResponse.getBody().containsKey("access_token")) {
                return "redirect:/?error=oauth_token_error";
            }

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 2. Get User Info
            String userInfoUrl = "https://api.github.com/user";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>("", authHeaders);
            
            ResponseEntity<Map> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class);
            
            if (userResponse.getBody() == null) {
                return "redirect:/?error=oauth_user_error";
            }
            
            Map userInfo = userResponse.getBody();
            String githubLogin = (String) userInfo.get("login"); // GitHub username
            // Integer githubId = (Integer) userInfo.get("id"); // GitHub ID

            // 3. Login or Register logic
            // 尝试查找是否已经绑定了 GitHub 的用户
            // 简化逻辑：直接用 GitHub username 作为系统 username 查找
            // 更好的做法是：User 表加一个 github_id 字段
            
            User user = userMapper.findByUsername(githubLogin);
            
            if (user == null) {
                // Register new user
                user = new User();
                user.setUsername(githubLogin);
                // 设置一个随机密码，因为是通过 OAuth 登录的，用户可能永远不需要用密码登录
                user.setPassword(UUID.randomUUID().toString());
                user.setRole("USER");
                user.setScore(0);
                user.setIsVip(false);
                user.setOauthProvider("github");
                
                // 如果需要避免用户名冲突，可以在这里处理，例如 githubLogin + "_gh"
                
                userMapper.insert(user);
                
                // 重新查询以获取生成的 ID
                user = userMapper.findByUsername(githubLogin);
            }

            // 4. Set Session
            session.setAttribute("user", user);

            return "redirect:/";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/?error=oauth_exception";
        }
    }
}
