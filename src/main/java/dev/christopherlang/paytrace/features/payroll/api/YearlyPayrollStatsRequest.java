package dev.christopherlang.paytrace.features.payroll.api;

import java.time.Year;

import com.fasterxml.jackson.annotation.JsonFormat;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsInterval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Yearly interval specific payload")
@Data
@EqualsAndHashCode(callSuper = true)
public class YearlyPayrollStatsRequest extends PayrollStatsRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Year startYear;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Year endYear;

    public YearlyPayrollStatsRequest() {
        this.setInterval(PayrollStatsInterval.YEARLY);
    }

}
