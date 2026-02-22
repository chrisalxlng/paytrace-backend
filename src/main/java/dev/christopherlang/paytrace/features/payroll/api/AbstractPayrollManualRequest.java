package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.util.List;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public abstract class AbstractPayrollManualRequest {

    @NotNull
    private BigDecimal payout;

    @NotNull
    @Valid
    private List<PayrollEntryRequest> entries;

    @Data
    public static class PayrollEntryRequest {

        @NotNull
        private PayrollEntryType type;

        @NotNull
        private BigDecimal amount;

    }

}
