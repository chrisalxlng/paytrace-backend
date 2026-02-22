package dev.christopherlang.paytrace.features.payroll.api;

import java.time.YearMonth;

import com.fasterxml.jackson.annotation.JsonFormat;

import dev.christopherlang.paytrace.common.NotFutureYearMonth;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreatePayrollRequest extends AbstractPayrollManualRequest {

    @NotNull
    @NotFutureYearMonth
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth accountingPeriod;

}
