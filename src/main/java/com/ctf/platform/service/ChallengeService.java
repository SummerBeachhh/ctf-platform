package com.ctf.platform.service;

import com.ctf.platform.entity.Category;
import com.ctf.platform.entity.Challenge;
import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllChallenges();
    List<Category> getAllCategories();
    Challenge getChallengeById(Integer id);
    List<Challenge> getChallengesByCategory(Integer categoryId);
    boolean verifyFlag(Integer id, String flag, Integer userId);
}
