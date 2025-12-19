package com.group.admin.req;

import lombok.Data;

@Data
public class AuthRegisterReq {
    private String email;
    private String password;
    private String nickname;
}
