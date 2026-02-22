package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record Payroll(
    UUID payrollId,
    String userId,
    YearMonth accountingPeriod,
    BigDecimal payout,
    BigDecimal consistencyDeviation,
    List<PayrollEntry> entries
) {

    public boolean hasDuplicateEntryTypes() {
        long uniqueTypesCount = entries.stream()
                .map(PayrollEntry::type)
                .distinct()
                .count();
        return uniqueTypesCount < entries.size();
    }

}
