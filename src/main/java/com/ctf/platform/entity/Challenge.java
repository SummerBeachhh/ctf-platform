package com.ctf.platform.entity;

import lombok.Data;

@Data
public class Challenge {
    private Integer id;
    private String title;
    private String description;
    private Integer categoryId;
    private Integer points;
    private String flag;
    private String attachmentUrl;
    private Integer sortOrder;
    private Boolean isVip; // 是否为 VIP 题目
    
    // 关联字段
    private String categoryName;
    
    // 非数据库字段，用于前端展示用户完成状态
    private Boolean isSolved;
}
