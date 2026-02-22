package dev.christopherlang.paytrace.features.payroll.api;

import java.time.YearMonth;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsInterval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Monthly interval specific payload")
@Data
@EqualsAndHashCode(callSuper = true)
public class MonthlyPayrollStatsRequest extends PayrollStatsRequest {

    @Schema(example = "2024-01", description = "ISO YearMonth format")
    private YearMonth startYearMonth;

    @Schema(example = "2024-12")
    private YearMonth endYearMonth;

    public MonthlyPayrollStatsRequest() {
        this.setInterval(PayrollStatsInterval.MONTHLY);
    }

}
