package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPayrollsResponse {

    private List<PayrollGroupDto> years;

    private boolean hasMore;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Year nextCursor;

    @Data
    @Builder
    public static class PayrollGroupDto {

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Year year;

        private int count;

        private BigDecimal sum;

        private List<PayrollSummaryDto> entries;

        @Data
        @Builder
        public static class PayrollSummaryDto {

            private UUID payrollId;

            private YearMonth accountingPeriod;

            private BigDecimal consistencyDeviation;

            private BigDecimal payout;

        }

    }

}
