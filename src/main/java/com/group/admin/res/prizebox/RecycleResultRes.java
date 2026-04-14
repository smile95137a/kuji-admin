package com.group.admin.res.prizebox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回收獎品結果回應
 *
 * @author Kuji Admin
 * @since 2026-03-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleResultRes {

    /**
     * 本次回收共獲得的 Bonus 點數
     */
    private Long totalBonus;

    /**
     * 本次回收件數
     */
    private Integer recycledCount;
}
