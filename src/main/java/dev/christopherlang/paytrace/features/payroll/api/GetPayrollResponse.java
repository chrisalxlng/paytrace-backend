package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetPayrollResponse {

    private UUID payrollId;

    private YearMonth accountingPeriod;

    private BigDecimal consistencyDeviation;

    private BigDecimal payout;

    private List<PayrollEntry> entries;

    @Data
    public static class PayrollEntry {

        private PayrollEntryType type;

        private BigDecimal amount;

    }

}
