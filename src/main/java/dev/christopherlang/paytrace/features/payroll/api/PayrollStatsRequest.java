package dev.christopherlang.paytrace.features.payroll.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollMetric;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsInterval;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "interval",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = MonthlyPayrollStatsRequest.class, name = "MONTHLY"),
    @JsonSubTypes.Type(value = YearlyPayrollStatsRequest.class, name = "YEARLY")
})
@Schema(
    description = "Payload for payroll statistics",
    subTypes = { MonthlyPayrollStatsRequest.class, YearlyPayrollStatsRequest.class }
)
@Data
public abstract class PayrollStatsRequest {

    private PayrollMetric metric;

    private PayrollStatsInterval interval;

}
