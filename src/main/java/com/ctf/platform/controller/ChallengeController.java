package com.ctf.platform.controller;

import com.ctf.platform.entity.Category;
import com.ctf.platform.entity.Challenge;
import com.ctf.platform.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;
    
    @Autowired
    private com.ctf.platform.mapper.UserMapper userMapper;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("challenges", challengeService.getAllChallenges());
        model.addAttribute("categories", challengeService.getAllCategories());
        model.addAttribute("rankings", userMapper.getRankings());
        return "index";
    }

    @PostMapping("/api/recharge")
    @ResponseBody
    public Map<String, Object> recharge(@RequestBody Map<String, Integer> payload) {
        Integer userId = payload.get("userId");
        userMapper.upgradeToVip(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "充值成功！您现在是 VIP 用户。");
        return result;
    }

    @GetMapping("/api/rankings")
    @ResponseBody
    public List<com.ctf.platform.entity.User> getRankings() {
        return userMapper.getRankings();
    }

    @GetMapping("/api/challenges")
    @ResponseBody
    public List<Challenge> getChallenges(@RequestParam(required = false) Integer categoryId) {
        if (categoryId != null) {
            return challengeService.getChallengesByCategory(categoryId);
        }
        return challengeService.getAllChallenges();
    }

    @PostMapping("/api/verify")
    @ResponseBody
    public Map<String, Object> verify(@RequestBody Map<String, Object> payload) {
        Integer id = Integer.parseInt(payload.get("id").toString());
        String flag = (String) payload.get("flag");
        // Default to user ID 1 if not provided (for demo/basic version)
        Integer userId = payload.containsKey("userId") ? Integer.parseInt(payload.get("userId").toString()) : 1;
        
        boolean success = challengeService.verifyFlag(id, flag, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Flag 正确！积分已添加。" : "Flag 错误，请重试。");
        return result;
    }
}
