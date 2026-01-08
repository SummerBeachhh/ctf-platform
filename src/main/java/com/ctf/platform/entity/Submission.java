package com.ctf.platform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Submission {
    private Integer id;
    private Integer userId;
    private Integer challengeId;
    private LocalDateTime submissionTime;
    private Boolean isCorrect;
}
