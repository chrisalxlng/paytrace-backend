package dev.christopherlang.paytrace.features.payroll.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeletePayrollRequest {

    @NotNull
    private UUID payrollId;

}
