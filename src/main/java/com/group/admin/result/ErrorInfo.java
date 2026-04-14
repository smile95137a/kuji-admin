package com.group.admin.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private String code;
    private String message;
    private Object details;

    public static ErrorInfo of(String code, String message) {
        return new ErrorInfo(code, message, null);
    }
}