package com.group.admin.result;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaInfo {
    private String timestamp;
    private String requestId;

    public static MetaInfo now() {
        return new MetaInfo(Instant.now().toString(), UUID.randomUUID().toString());
    }
}