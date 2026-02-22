package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayrollGroups {

    List<PayrollGroup> groups;

    boolean hasMore;

    Year lowestYearAvailable;

    @Value
    @Builder
    public static class PayrollGroup {

        Year year;

        int count;

        BigDecimal sum;

        List<PayrollSummary> entries;

        @Value
        @Builder
        public static class PayrollSummary {

            UUID payrollId;

            YearMonth accountingPeriod;

            BigDecimal consistencyDeviation;

            BigDecimal payout;

        }

    }

}
