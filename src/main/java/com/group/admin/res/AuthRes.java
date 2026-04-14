package com.group.admin.res;

import lombok.Data;

@Data
public class AuthRes {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Object user;
    /** OAuth 新用戶首次登入時為 true，前端用於顯示補碼引導 */
    private Boolean isNewUser;
}
