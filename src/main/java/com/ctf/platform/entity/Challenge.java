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
    
    // 关联字段
    private String categoryName;
}
