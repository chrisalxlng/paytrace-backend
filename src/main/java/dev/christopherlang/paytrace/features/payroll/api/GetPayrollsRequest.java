package dev.christopherlang.paytrace.features.payroll.api;

import java.time.Year;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GetPayrollsRequest {

    private Boolean consistent;

    @Positive
    private int limit = 3;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Year cursor;

}
