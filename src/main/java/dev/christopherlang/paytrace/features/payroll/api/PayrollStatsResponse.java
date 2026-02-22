package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollMetric;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsInterval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PayrollStatsResponse {

    PayrollMetric metric;

    PayrollStatsInterval interval;

    BigDecimal sum;

    BigDecimal average;

    List<YearlyStatsResponse> years;

    @Value
    @Builder
    public static class YearlyStatsResponse {

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Year year;

        BigDecimal sum;

        BigDecimal cumulativeSum;

        BigDecimal average;

        BigDecimal yoyPercentage;

        List<MonthlyStatsResponse> months;

    }

    @Value
    @Builder
    public static class MonthlyStatsResponse {

        @Schema(example = "2023-01")
        YearMonth yearMonth;

        BigDecimal value;

        BigDecimal yoyPercentage;

        BigDecimal momPercentage;

    }

}
