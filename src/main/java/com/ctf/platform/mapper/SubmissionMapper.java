package com.ctf.platform.mapper;

import com.ctf.platform.entity.Submission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SubmissionMapper {

    @Insert("INSERT INTO submission (user_id, challenge_id, is_correct) VALUES (#{userId}, #{challengeId}, #{isCorrect})")
    void insert(Submission submission);

    @Select("SELECT * FROM submission WHERE user_id = #{userId} AND challenge_id = #{challengeId} AND is_correct = TRUE")
    List<Submission> findCorrectByUserIdAndChallengeId(Integer userId, Integer challengeId);

    @Select("SELECT DISTINCT challenge_id FROM submission WHERE user_id = #{userId} AND is_correct = TRUE")
    List<Integer> findSolvedChallengeIdsByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM submission")
    int count();
}
