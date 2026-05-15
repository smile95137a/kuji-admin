package com.group.admin.req.store;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "結構化營業時間")
public class BusinessHoursReq {

    @NotNull
    @Valid
    @Schema(description = "每週日程清單")
    private List<DaySchedule> schedules;

    @Valid
    @Schema(description = "例外日期設定（例如店休或臨時調整）")
    private List<ExceptionDay> exceptions;

    @Schema(description = "時區（選填）", example = "Asia/Taipei")
    private String tz;

    @Data
    @Schema(description = "每日時段")
    public static class DaySchedule {

        @NotNull
        @Schema(description = "星期，使用 MON|TUE|WED|THU|FRI|SAT|SUN", example = "MON")
        private String day;

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "時間格式需為 HH:mm")
        @Schema(description = "開門時間（HH:mm）", example = "10:00")
        private String open;

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "時間格式需為 HH:mm")
        @Schema(description = "打烊時間（HH:mm）", example = "22:00")
        private String close;

        @Schema(description = "是否整日店休（若為 true 則忽略 open/close）")
        private Boolean closed;
    }

    @Data
    @Schema(description = "例外日期設定，例如國定假日店休或臨時營業時間")
    public static class ExceptionDay {

        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式需為 YYYY-MM-DD")
        @Schema(description = "日期（YYYY-MM-DD）", example = "2026-12-25")
        private String date;

        @Schema(description = "該日是否店休")
        private Boolean closed;

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "時間格式需為 HH:mm")
        @Schema(description = "開門時間（覆蓋日常設定）", example = "11:00")
        private String open;

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "時間格式需為 HH:mm")
        @Schema(description = "打烊時間（覆蓋日常設定）", example = "18:00")
        private String close;
    }
}
