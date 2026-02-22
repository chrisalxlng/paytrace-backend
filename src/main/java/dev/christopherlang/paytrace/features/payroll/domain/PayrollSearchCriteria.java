package dev.christopherlang.paytrace.features.payroll.domain;

import java.time.Year;

import lombok.Value;

@Value
public class PayrollSearchCriteria {

    Boolean consistent;

    int yearLimit;

    Year beforeYear;

}
