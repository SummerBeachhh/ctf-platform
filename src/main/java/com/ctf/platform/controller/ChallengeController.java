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

    @Autowired
    private com.ctf.platform.mapper.SubmissionMapper submissionMapper;

    @GetMapping("/admin")
    public String admin(Model model, jakarta.servlet.http.HttpSession session) {
        com.ctf.platform.entity.User user = (com.ctf.platform.entity.User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        
        model.addAttribute("categories", challengeService.getAllCategories());
        model.addAttribute("totalUsers", userMapper.count());
        model.addAttribute("totalChallenges", challengeService.getTotalChallenges());
        model.addAttribute("totalSubmissions", submissionMapper.count());
        
        return "admin";
    }

    @GetMapping("/")
    public String index(Model model, jakarta.servlet.http.HttpSession session, @RequestParam(defaultValue = "1") int page) {
        com.ctf.platform.entity.User user = (com.ctf.platform.entity.User) session.getAttribute("user");
        
        int pageSize = 5;
        List<Challenge> challenges = challengeService.getChallengesByPage(page, pageSize);
        processChallenges(challenges, user);

        int totalChallenges = challengeService.getTotalChallenges();
        int totalPages = (int) Math.ceil((double) totalChallenges / pageSize);

        model.addAttribute("challenges", challenges);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("categories", challengeService.getAllCategories());
        model.addAttribute("rankings", userMapper.getRankings());
        return "index";
    }

    private void processChallenges(List<Challenge> challenges, com.ctf.platform.entity.User user) {
        boolean isVip = user != null && Boolean.TRUE.equals(user.getIsVip());
        List<Integer> solvedIds = (user != null) ? challengeService.getSolvedChallengeIds(user.getId()) : java.util.Collections.emptyList();

        for (Challenge c : challenges) {
            // 清除 flag，避免泄露
            c.setFlag(null);
            
            if (Boolean.TRUE.equals(c.getIsVip()) && !isVip) {
                c.setDescription("此内容仅限 VIP 会员查看。加入 VIP 解锁更多精彩题目！");
                c.setAttachmentUrl(null);
            }

            if (solvedIds.contains(c.getId())) {
                c.setIsSolved(true);
            } else {
                c.setIsSolved(false);
            }
        }
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
    public Map<String, Object> getChallenges(@RequestParam(required = false) Integer categoryId, 
                                             @RequestParam(required = false) String search,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "5") int size,
                                             jakarta.servlet.http.HttpSession session) {
        com.ctf.platform.entity.User user = (com.ctf.platform.entity.User) session.getAttribute("user");

        List<Challenge> challenges;
        int totalChallenges;
        
        if (categoryId != null) {
            challenges = challengeService.getChallengesByCategoryPage(categoryId, page, size);
            totalChallenges = challengeService.getTotalChallengesByCategory(categoryId);
        } else if (search != null && !search.trim().isEmpty()) {
            challenges = challengeService.getChallengesBySearchPage(search, page, size);
            totalChallenges = challengeService.getTotalChallengesBySearch(search);
        } else {
            challenges = challengeService.getChallengesByPage(page, size);
            totalChallenges = challengeService.getTotalChallenges();
        }
        
        int totalPages = (int) Math.ceil((double) totalChallenges / size);

        processChallenges(challenges, user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("challenges", challenges);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        
        return result;
    }

    @PostMapping("/api/verify")
    @ResponseBody
    public Map<String, Object> verify(@RequestBody Map<String, Object> payload, jakarta.servlet.http.HttpSession session) {
        Integer id = Integer.parseInt(payload.get("id").toString());
        String flag = (String) payload.get("flag");
        
        com.ctf.platform.entity.User user = (com.ctf.platform.entity.User) session.getAttribute("user");
        Integer userId;
        
        if (user != null) {
            userId = user.getId();
        } else if (payload.containsKey("userId")) {
            // Fallback for demo/testing without login
            userId = Integer.parseInt(payload.get("userId").toString());
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录！");
            return result;
        }

        // Check VIP permission
        Challenge challenge = challengeService.getChallengeById(id);
        if (challenge != null && Boolean.TRUE.equals(challenge.getIsVip())) {
             // Reload user to check latest VIP status
             if (user != null) {
                 user = userMapper.findById(user.getId());
             }
             if (user == null || !Boolean.TRUE.equals(user.getIsVip())) {
                 Map<String, Object> result = new HashMap<>();
                 result.put("success", false);
                 result.put("message", "此题目仅限 VIP 会员挑战！");
                 return result;
             }
        }
        
        try {
            boolean success = challengeService.verifyFlag(id, flag, userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "Flag 正确！积分已添加。" : "Flag 错误，请重试。");
            return result;
        } catch (RuntimeException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
}
