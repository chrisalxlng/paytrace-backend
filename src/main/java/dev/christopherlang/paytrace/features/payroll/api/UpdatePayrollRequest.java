package dev.christopherlang.paytrace.features.payroll.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdatePayrollRequest extends AbstractPayrollManualRequest {

    @NotNull
    private UUID payrollId;

}
