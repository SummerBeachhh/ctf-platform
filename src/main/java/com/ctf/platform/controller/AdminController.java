package com.ctf.platform.controller;

import com.ctf.platform.entity.Challenge;
import com.ctf.platform.entity.User;
import com.ctf.platform.mapper.ChallengeMapper;
import com.ctf.platform.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChallengeMapper challengeMapper;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/users")
    public List<User> getAllUsers(HttpSession session) {
        if (!isAdmin(session)) return null;
        return userMapper.findAll();
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "未授权");
            return result;
        }
        userMapper.deleteById(id);
        result.put("success", true);
        return result;
    }

    @PostMapping("/challenges")
    public Map<String, Object> addChallenge(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("flag") String flag,
            @RequestParam("points") Integer points,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "未授权");
            return result;
        }

        Challenge challenge = new Challenge();
        challenge.setTitle(title);
        challenge.setDescription(description);
        challenge.setFlag(flag);
        challenge.setPoints(points);
        challenge.setCategoryId(categoryId);

        if (file != null && !file.isEmpty()) {
            try {
                // Use absolute path
                String projectRoot = System.getProperty("user.dir");
                String uploadDir = projectRoot + File.separator + "uploads" + File.separator;
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File dest = new File(dir, fileName);
                file.transferTo(dest);
                challenge.setAttachmentUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                result.put("success", false);
                result.put("message", "文件上传失败: " + e.getMessage());
                return result;
            }
        }

        challengeMapper.insert(challenge);
        result.put("success", true);
        return result;
    }

    @DeleteMapping("/challenges/{id}")
    public Map<String, Object> deleteChallenge(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "未授权");
            return result;
        }
        challengeMapper.deleteById(id);
        result.put("success", true);
        return result;
    }

    @GetMapping("/challenges/{id}")
    public Map<String, Object> getChallenge(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "未授权");
            return result;
        }
        Challenge challenge = challengeMapper.findById(id);
        if (challenge == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }
        result.put("success", true);
        result.put("data", challenge);
        return result;
    }

    @PostMapping("/challenges/{id}/sort")
    public Map<String, Object> updateSortOrder(@PathVariable Integer id, @RequestParam Integer sortOrder, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "未授权");
            return result;
        }
        challengeMapper.updateSortOrder(id, sortOrder);
        result.put("success", true);
        return result;
    }

    @PutMapping("/challenges/{id}")
    public Map<String, Object> updateChallenge(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) {
            result.put("success", false);
            result.put("message", "未授权");
            return result;
        }

        Challenge challenge = challengeMapper.findById(id);
        if (challenge == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }

        challenge.setTitle((String) payload.get("title"));
        challenge.setDescription((String) payload.get("description"));
        challenge.setFlag((String) payload.get("flag"));
        // Handle points as Integer (JSON numbers might be Integer or Double)
        Object pointsObj = payload.get("points");
        if (pointsObj instanceof Integer) {
            challenge.setPoints((Integer) pointsObj);
        } else if (pointsObj instanceof String) {
            challenge.setPoints(Integer.parseInt((String) pointsObj));
        }
        
        Object catObj = payload.get("categoryId");
        if (catObj instanceof Integer) {
            challenge.setCategoryId((Integer) catObj);
        } else if (catObj instanceof String) {
            challenge.setCategoryId(Integer.parseInt((String) catObj));
        }

        challengeMapper.update(challenge);
        result.put("success", true);
        return result;
    }
}
