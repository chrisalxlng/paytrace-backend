package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

public enum PayrollMetric {

    GROSS_INCOME(List.of(PayrollEntryType.BASE_SALARY, PayrollEntryType.BONUS_SALARY), CalculationMode.SUM),
    GROSS_BASE_SALARY(List.of(PayrollEntryType.BASE_SALARY), CalculationMode.SUM),
    GROSS_BONUS_SALARY(List.of(PayrollEntryType.BONUS_SALARY), CalculationMode.SUM),

    NET_INCOME(
        Stream.concat(
            allDeductionTypes().stream(),
            Stream.of(PayrollEntryType.BASE_SALARY, PayrollEntryType.BONUS_SALARY)
        ).toList(),
        CalculationMode.NET_LOGIC
    ),
    NET_BASE_SALARY(
        Stream.concat(
            allBaseDeductionTypes().stream(),
            Stream.of(PayrollEntryType.BASE_SALARY)
        ).toList(),
        CalculationMode.NET_LOGIC
    ),
    NET_BONUS_SALARY(
        Stream.concat(
            allBonusDeductionTypes().stream(),
            Stream.of(PayrollEntryType.BONUS_SALARY)
        ).toList(),
        CalculationMode.NET_LOGIC
    ),

    DEDUCTIONS(allDeductionTypes(), CalculationMode.SUM),
    TAXES(allTaxTypes(), CalculationMode.SUM),
    INCOME_TAX(List.of(PayrollEntryType.BASE_INCOME_TAX, PayrollEntryType.BONUS_INCOME_TAX), CalculationMode.SUM),
    CHURCH_TAX(List.of(PayrollEntryType.BASE_CHURCH_TAX, PayrollEntryType.BONUS_CHURCH_TAX), CalculationMode.SUM),
    SOLIDARITY_SURCHARGE(List.of(PayrollEntryType.BASE_SOLIDARITY_SURCHARGE, PayrollEntryType.BONUS_SOLIDARITY_SURCHARGE), CalculationMode.SUM),
    INSURANCE_DEDUCTIONS(allInsuranceTypes(), CalculationMode.SUM),
    HEALTH_INSURANCE(List.of(PayrollEntryType.BASE_HEALTH_INSURANCE, PayrollEntryType.BONUS_HEALTH_INSURANCE), CalculationMode.SUM),
    PENSION_INSURANCE(List.of(PayrollEntryType.BASE_PENSION_INSURANCE, PayrollEntryType.BONUS_PENSION_INSURANCE), CalculationMode.SUM),
    UNEMPLOYMENT_INSURANCE(List.of(PayrollEntryType.BASE_UNEMPLOYMENT_INSURANCE, PayrollEntryType.BONUS_UNEMPLOYMENT_INSURANCE), CalculationMode.SUM),
    LONG_TERM_CARE_INSURANCE(List.of(PayrollEntryType.BASE_LONG_TERM_CARE_INSURANCE, PayrollEntryType.BONUS_LONG_TERM_CARE_INSURANCE), CalculationMode.SUM);

    private final List<PayrollEntryType> types;
    private final CalculationMode calculationMode;

    PayrollMetric(List<PayrollEntryType> types, CalculationMode calculationMode) {
        this.types = types;
        this.calculationMode = calculationMode;
    }

    public BigDecimal calculateContribution(PayrollEntryType type, BigDecimal amount) {
        if (this.calculationMode == CalculationMode.NET_LOGIC && type.isDeduction()) {
            return amount.negate();
        }
        return amount;
    }

    private enum CalculationMode { SUM, NET_LOGIC }

    private static List<PayrollEntryType> allDeductionTypes() {
        return Stream.concat(allTaxTypes().stream(), allInsuranceTypes().stream()).toList();
    }

    private static List<PayrollEntryType> allBaseDeductionTypes() {
        return Stream.concat(allBaseTaxTypes().stream(), allBaseInsuranceTypes().stream()).toList();
    }

    private static List<PayrollEntryType> allBonusDeductionTypes() {
        return Stream.concat(allBonusTaxTypes().stream(), allBonusInsuranceTypes().stream()).toList();
    }

    private static List<PayrollEntryType> allTaxTypes() {
        return Stream.concat(allBaseTaxTypes().stream(), allBonusTaxTypes().stream()).toList();
    }

    private static List<PayrollEntryType> allBaseTaxTypes() {
        return List.of(PayrollEntryType.BASE_INCOME_TAX, PayrollEntryType.BASE_CHURCH_TAX, PayrollEntryType.BASE_SOLIDARITY_SURCHARGE);
    }

    private static List<PayrollEntryType> allBonusTaxTypes() {
        return List.of(PayrollEntryType.BONUS_INCOME_TAX, PayrollEntryType.BONUS_CHURCH_TAX, PayrollEntryType.BONUS_SOLIDARITY_SURCHARGE);
    }

    private static List<PayrollEntryType> allInsuranceTypes() {
        return Stream.concat(allBaseInsuranceTypes().stream(), allBonusInsuranceTypes().stream()).toList();
    }

    private static List<PayrollEntryType> allBaseInsuranceTypes() {
        return List.of(PayrollEntryType.BASE_HEALTH_INSURANCE, PayrollEntryType.BASE_PENSION_INSURANCE,
                       PayrollEntryType.BASE_UNEMPLOYMENT_INSURANCE, PayrollEntryType.BASE_LONG_TERM_CARE_INSURANCE);
    }

    private static List<PayrollEntryType> allBonusInsuranceTypes() {
        return List.of(PayrollEntryType.BONUS_HEALTH_INSURANCE, PayrollEntryType.BONUS_PENSION_INSURANCE,
                       PayrollEntryType.BONUS_UNEMPLOYMENT_INSURANCE, PayrollEntryType.BONUS_LONG_TERM_CARE_INSURANCE);
    }

    public List<PayrollEntryType> getTypes() { return types; }

}
