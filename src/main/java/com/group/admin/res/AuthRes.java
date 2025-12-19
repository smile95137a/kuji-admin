package com.group.admin.res;

import lombok.Data;

@Data
public class AuthRes {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Object user;
}
