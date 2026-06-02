package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record Payroll(
    UUID payrollId,
    String userId,
    YearMonth accountingPeriod,
    BigDecimal payout,
    BigDecimal consistencyDeviation,
    List<PayrollEntry> entries
) {

    public Payroll {
        if (entries == null) {
            entries = List.of();
        }
    }

    public Payroll withUserId(String userId) {
        return this.toBuilder()
            .userId(userId)
            .build();
    }

    public Payroll withConsistencyDeviation(BigDecimal consistencyDeviation) {
        return this.toBuilder()
            .consistencyDeviation(consistencyDeviation)
            .build();
    }

    public Payroll withAccountingPeriod(YearMonth accountingPeriod) {
        return this.toBuilder()
            .accountingPeriod(accountingPeriod)
            .build();
    }

    public Payroll withPayout(BigDecimal payout) {
        return this.toBuilder()
            .payout(payout)
            .build();
    }

    public Payroll withEntries(List<PayrollEntry> entries) {
        return this.toBuilder()
            .entries(entries)
            .build();
    }

    public boolean hasDuplicateEntryTypes() {
        long uniqueTypesCount = entries.stream()
                .map(PayrollEntry::type)
                .distinct()
                .count();
        return uniqueTypesCount < entries.size();
    }

}
