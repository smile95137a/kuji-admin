package com.group.admin.result;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MetaInfo {
    private String timestamp;
    private String requestId;
    
    public static MetaInfo now() {
        return MetaInfo.builder()
            .timestamp(Instant.now().toString())
            .requestId(UUID.randomUUID().toString())
            .build();
    }
}