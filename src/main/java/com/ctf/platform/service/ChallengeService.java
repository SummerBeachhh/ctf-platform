package com.ctf.platform.service;

import com.ctf.platform.entity.Category;
import com.ctf.platform.entity.Challenge;
import java.util.List;

public interface ChallengeService {
    List<Challenge> getAllChallenges();
    List<Challenge> getChallengesByPage(int page, int size);
    int getTotalChallenges();
    List<Challenge> getChallengesBySearchPage(String search, int page, int size);
    int getTotalChallengesBySearch(String search);
    List<Challenge> getChallengesByCategoryPage(int categoryId, int page, int size);
    int getTotalChallengesByCategory(int categoryId);
    List<Category> getAllCategories();
    Challenge getChallengeById(Integer id);
    List<Challenge> getChallengesByCategory(Integer categoryId);
    boolean verifyFlag(Integer id, String flag, Integer userId);
    List<Integer> getSolvedChallengeIds(Integer userId);
}
