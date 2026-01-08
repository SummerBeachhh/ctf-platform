package com.ctf.platform.service.impl;

import com.ctf.platform.entity.Category;
import com.ctf.platform.entity.Challenge;
import com.ctf.platform.entity.Submission;
import com.ctf.platform.mapper.ChallengeMapper;
import com.ctf.platform.mapper.SubmissionMapper;
import com.ctf.platform.mapper.UserMapper;
import com.ctf.platform.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    @Autowired
    private ChallengeMapper challengeMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Challenge> getAllChallenges() {
        return challengeMapper.findAll();
    }

    @Override
    public List<Challenge> getChallengesByPage(int page, int size) {
        int offset = (page - 1) * size;
        return challengeMapper.findPage(offset, size);
    }

    @Override
    public int getTotalChallenges() {
        return challengeMapper.countAll();
    }

    @Override
    public List<Challenge> getChallengesBySearchPage(String search, int page, int size) {
        int offset = (page - 1) * size;
        return challengeMapper.findPageBySearch(search, offset, size);
    }

    @Override
    public int getTotalChallengesBySearch(String search) {
        return challengeMapper.countBySearch(search);
    }

    @Override
    public List<Challenge> getChallengesByCategoryPage(int categoryId, int page, int size) {
        int offset = (page - 1) * size;
        return challengeMapper.findPageByCategoryId(categoryId, offset, size);
    }

    @Override
    public int getTotalChallengesByCategory(int categoryId) {
        return challengeMapper.countByCategoryId(categoryId);
    }

    @Override
    public List<Category> getAllCategories() {
        return challengeMapper.findAllCategories();
    }

    @Override
    public Challenge getChallengeById(Integer id) {
        return challengeMapper.findById(id);
    }

    @Override
    public List<Challenge> getChallengesByCategory(Integer categoryId) {
        return challengeMapper.findByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public boolean verifyFlag(Integer challengeId, String flag, Integer userId) {
        Challenge challenge = challengeMapper.findById(challengeId);
        if (challenge == null) {
            return false;
        }

        // Check if already solved BEFORE inserting the new submission
        List<Submission> previousSolves = submissionMapper.findCorrectByUserIdAndChallengeId(userId, challengeId);
        if (!previousSolves.isEmpty()) {
            throw new RuntimeException("您已完成该题目，无法重复提交！");
        }

        boolean isCorrect = challenge.getFlag().trim().equals(flag.trim());

        // Record Submission
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setChallengeId(challengeId);
        submission.setIsCorrect(isCorrect);
        submissionMapper.insert(submission);

        // Add points if correct
        if (isCorrect) {
            userMapper.addScore(userId, challenge.getPoints());
        }

        return isCorrect;
    }

    @Override
    public List<Integer> getSolvedChallengeIds(Integer userId) {
        return submissionMapper.findSolvedChallengeIdsByUserId(userId);
    }
}
