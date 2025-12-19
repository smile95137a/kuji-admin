package com.group.admin.req;

import lombok.Data;

@Data
public class AuthLoginReq {
    private String email;
    private String password;
}
