package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.time.YearMonth;

public record RawPayrollData(
    YearMonth accountingPeriod,
    PayrollEntryType type,
    BigDecimal amount
) {}
