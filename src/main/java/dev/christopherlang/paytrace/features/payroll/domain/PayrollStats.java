package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import lombok.Value;
import lombok.Builder;

@Value
@Builder
public class PayrollStats {

    PayrollMetric metric;

    PayrollStatsInterval interval;

    BigDecimal totalSum;

    BigDecimal overallAverage;

    List<YearlyStats> yearlyBreakdown;

    @Value
    @Builder
    public static class YearlyStats {

        Year year;

        BigDecimal yearlySum;

        BigDecimal yearlyAverage;

        BigDecimal yearlyCumulativeSum;

        BigDecimal yearOverYearGrowth;

        List<MonthlyStats> monthlyBreakdown;

    }

    @Value
    @Builder
    public static class MonthlyStats {

        YearMonth yearMonth;

        BigDecimal value;

        BigDecimal yearOverYearGrowth;

        BigDecimal monthOverMonthGrowth;

    }

}

