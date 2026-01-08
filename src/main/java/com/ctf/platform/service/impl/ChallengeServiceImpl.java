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

        boolean isCorrect = challenge.getFlag().trim().equals(flag.trim());

        // Check if already solved BEFORE inserting the new submission
        boolean alreadySolved = false;
        if (isCorrect) {
            List<Submission> previousSolves = submissionMapper.findCorrectByUserIdAndChallengeId(userId, challengeId);
            if (!previousSolves.isEmpty()) {
                alreadySolved = true;
            }
        }

        // Record Submission
        Submission submission = new Submission();
        submission.setUserId(userId);
        submission.setChallengeId(challengeId);
        submission.setIsCorrect(isCorrect);
        submissionMapper.insert(submission);

        // Add points if correct and first time
        if (isCorrect && !alreadySolved) {
            userMapper.addScore(userId, challenge.getPoints());
        }

        return isCorrect;
    }
}
