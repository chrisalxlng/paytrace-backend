package dev.christopherlang.paytrace.features.payroll.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayrollEntryType {

    BASE_SALARY(false, true),
    BASE_INCOME_TAX(true, true),
    BASE_CHURCH_TAX(true, true),
    BASE_SOLIDARITY_SURCHARGE(true, true),
    BASE_HEALTH_INSURANCE(true, true),
    BASE_PENSION_INSURANCE(true, true),
    BASE_UNEMPLOYMENT_INSURANCE(true, true),
    BASE_LONG_TERM_CARE_INSURANCE(true, true),

    BONUS_SALARY(false, true),
    BONUS_INCOME_TAX(true, true),
    BONUS_CHURCH_TAX(true, true),
    BONUS_SOLIDARITY_SURCHARGE(true, true),
    BONUS_HEALTH_INSURANCE(true, true),
    BONUS_PENSION_INSURANCE(true, true),
    BONUS_UNEMPLOYMENT_INSURANCE(true, true),
    BONUS_LONG_TERM_CARE_INSURANCE(true, true),

    REIMBURSEMENTS(false, false),
    DISBURSEMENTS(true, false);

    private final boolean deduction;

    private final boolean gross;

    public boolean isIncome() {
        return !deduction;
    }

    public boolean isNet() {
        return !gross;
    }

}
