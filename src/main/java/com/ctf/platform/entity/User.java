package com.ctf.platform.entity;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private Integer score;
    private Boolean isVip;
    private String oauthProvider;
}
