package com.group.admin.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    private String code;
    private String message;
    private Object details;
    
    public static ErrorInfo of(String code, String message) {
        return ErrorInfo.builder()
            .code(code)
            .message(message)
            .build();
    }
}