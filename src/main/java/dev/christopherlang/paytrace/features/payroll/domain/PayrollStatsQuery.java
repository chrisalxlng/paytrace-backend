package dev.christopherlang.paytrace.features.payroll.domain;

import java.time.Year;
import java.time.YearMonth;

import lombok.Value;

@Value
public class PayrollStatsQuery {

    PayrollMetric metric;

    PayrollStatsInterval interval;

    YearMonth startYearMonth;

    YearMonth endYearMonth;

    Year startYear;

    Year endYear;

}
