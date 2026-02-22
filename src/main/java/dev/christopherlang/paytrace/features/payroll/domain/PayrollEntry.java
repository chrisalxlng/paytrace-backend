package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record PayrollEntry(
    PayrollEntryType type,
    BigDecimal amount
) {}
