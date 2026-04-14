package com.group.admin.res.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRes {
    private String userId;
    private Long goldBalance;
    private Long bonusBalance;
    private Long totalRecharged;
}
