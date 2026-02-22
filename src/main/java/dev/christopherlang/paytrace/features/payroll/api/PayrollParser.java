package dev.christopherlang.paytrace.features.payroll.api;

import dev.christopherlang.paytrace.features.payroll.domain.Payroll;

public interface PayrollParser {

    boolean supports(String pdfText);

    Payroll parse(String pdfText);

}
