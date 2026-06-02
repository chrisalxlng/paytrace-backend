package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import dev.christopherlang.paytrace.common.Range;
import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntry;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;

@Component
public class DatevPayrollParserProvider implements PayrollParser {

    private static final String PROVIDER_KEYWORD = "datev";

    private static final Range GROSS_SALARY_RANGE = new Range(7, 23);
    private static final Range INCOME_TAX_RANGE = new Range(23, 39);
    private static final Range CHURCH_TAX_RANGE = new Range(39, 55);
    private static final Range SOLIDARITY_SURCHARGE_RANGE = new Range(55, 71);

    private static final Range HEALTH_INSURANCE_RANGE = new Range(40, 50);
    private static final Range PENSION_INSURANCE_RANGE = new Range(50, 59);
    private static final Range UNEMPLOYMENT_INSURANCE_RANGE = new Range(59, 69);
    private static final Range LONG_TERM_INSURANCE_RANGE = new Range(69, 80);

    @Override
    public boolean supports(String pdfText) {
        return normalize(pdfText).contains(PROVIDER_KEYWORD);
    }

    @Override
    public Payroll parse(String pdfText) {
        String[] lines = normalize(pdfText).split("\n");

        BigDecimal grossSalaryBase = BigDecimal.ZERO;
        BigDecimal grossSalaryBonus = BigDecimal.ZERO;

        BigDecimal incomeTaxBase = BigDecimal.ZERO;
        BigDecimal incomeTaxBonus = BigDecimal.ZERO;
        BigDecimal churchTaxBase = BigDecimal.ZERO;
        BigDecimal churchTaxBonus = BigDecimal.ZERO;
        BigDecimal solidaritySurchargeBase = BigDecimal.ZERO;
        BigDecimal solidaritySurchargeBonus = BigDecimal.ZERO;

        BigDecimal healthInsuranceBase = BigDecimal.ZERO;
        BigDecimal healthInsuranceBonus = BigDecimal.ZERO;
        BigDecimal pensionInsuranceBase = BigDecimal.ZERO;
        BigDecimal pensionInsuranceBonus = BigDecimal.ZERO;
        BigDecimal unemploymentInsuranceBase = BigDecimal.ZERO;
        BigDecimal unemploymentInsuranceBonus = BigDecimal.ZERO;
        BigDecimal longTermCareInsuranceBase = BigDecimal.ZERO;
        BigDecimal longTermCareInsuranceBonus = BigDecimal.ZERO;

        BigDecimal payout = BigDecimal.ZERO;
        YearMonth accountingPeriod = null;

        boolean taxLineHandled = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.stripLeading().startsWith("für")) {
                Pattern pattern = Pattern.compile(
                    "^\\s*für\\s+(\\p{L}+\\s+\\d{4}).*?\\s*$",
                    Pattern.CASE_INSENSITIVE
                );
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String monthYearString = matcher.group(1);

                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("MMMM yyyy")
                        .toFormatter(Locale.GERMAN);
                    YearMonth yearMonth = YearMonth.parse(
                        monthYearString,
                        formatter
                    );

                    accountingPeriod = yearMonth;
                }
            }

            if (i == lines.length - 1) {
                String normalized = line.trim().replaceAll("\\s+", " ");
                String amountString = normalized.substring(normalized.lastIndexOf(" ") + 1);

                payout = new BigDecimal(
                    amountString
                        .replace(".", "")
                        .replace(",", ".")
                );
            }

            if (line.startsWith("l  ") && taxLineHandled) {
                healthInsuranceBase = extractDecimal(line.substring(HEALTH_INSURANCE_RANGE.min(), HEALTH_INSURANCE_RANGE.max()));
                pensionInsuranceBase = extractDecimal(line.substring(PENSION_INSURANCE_RANGE.min(), PENSION_INSURANCE_RANGE.max()));
                unemploymentInsuranceBase = extractDecimal(line.substring(UNEMPLOYMENT_INSURANCE_RANGE.min(), UNEMPLOYMENT_INSURANCE_RANGE.max()));
                longTermCareInsuranceBase = extractDecimal(line.substring(LONG_TERM_INSURANCE_RANGE.min(), LONG_TERM_INSURANCE_RANGE.max()));
            }

            if (line.startsWith("l  ") && !taxLineHandled) {
                grossSalaryBase = extractDecimal(line.substring(GROSS_SALARY_RANGE.min(), GROSS_SALARY_RANGE.max()));
                incomeTaxBase = extractDecimal(line.substring(INCOME_TAX_RANGE.min(), INCOME_TAX_RANGE.max()));
                churchTaxBase = extractDecimal(line.substring(CHURCH_TAX_RANGE.min(), CHURCH_TAX_RANGE.max()));
                solidaritySurchargeBase = extractDecimal(line.substring(SOLIDARITY_SURCHARGE_RANGE.min(), SOLIDARITY_SURCHARGE_RANGE.max()));
                taxLineHandled = true;
            }

            if (line.startsWith("s  ")) {
                grossSalaryBase = extractDecimal(line.substring(GROSS_SALARY_RANGE.min(), GROSS_SALARY_RANGE.max()));
                incomeTaxBase = extractDecimal(line.substring(INCOME_TAX_RANGE.min(), INCOME_TAX_RANGE.max()));
                churchTaxBase = extractDecimal(line.substring(CHURCH_TAX_RANGE.min(), CHURCH_TAX_RANGE.max()));
                solidaritySurchargeBase = extractDecimal(line.substring(SOLIDARITY_SURCHARGE_RANGE.min(), SOLIDARITY_SURCHARGE_RANGE.max()));
            }

            if (line.startsWith("e  ")) {
                healthInsuranceBase = extractDecimal(line.substring(HEALTH_INSURANCE_RANGE.min(), HEALTH_INSURANCE_RANGE.max()));
                pensionInsuranceBase = extractDecimal(line.substring(PENSION_INSURANCE_RANGE.min(), PENSION_INSURANCE_RANGE.max()));
                unemploymentInsuranceBase = extractDecimal(line.substring(UNEMPLOYMENT_INSURANCE_RANGE.min(), UNEMPLOYMENT_INSURANCE_RANGE.max()));
                longTermCareInsuranceBase = extractDecimal(line.substring(LONG_TERM_INSURANCE_RANGE.min(), LONG_TERM_INSURANCE_RANGE.max()));
            }
        }

        return Payroll.builder()
                .entries(List.of(
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_SALARY)
                        .amount(grossSalaryBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_SALARY)
                        .amount(grossSalaryBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_INCOME_TAX)
                        .amount(incomeTaxBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_INCOME_TAX)
                        .amount(incomeTaxBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_CHURCH_TAX)
                        .amount(churchTaxBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_CHURCH_TAX)
                        .amount(churchTaxBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_SOLIDARITY_SURCHARGE)
                        .amount(solidaritySurchargeBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_SOLIDARITY_SURCHARGE)
                        .amount(solidaritySurchargeBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_HEALTH_INSURANCE)
                        .amount(healthInsuranceBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_HEALTH_INSURANCE)
                        .amount(healthInsuranceBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_PENSION_INSURANCE)
                        .amount(pensionInsuranceBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_PENSION_INSURANCE)
                        .amount(pensionInsuranceBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_UNEMPLOYMENT_INSURANCE)
                        .amount(unemploymentInsuranceBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_UNEMPLOYMENT_INSURANCE)
                        .amount(unemploymentInsuranceBonus)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BASE_LONG_TERM_CARE_INSURANCE)
                        .amount(longTermCareInsuranceBase)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.BONUS_LONG_TERM_CARE_INSURANCE)
                        .amount(longTermCareInsuranceBonus)
                        .build()
                    )
                )
                .accountingPeriod(accountingPeriod)
                .payout(payout)
                .build();
    }

    private BigDecimal extractDecimal(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(digits).movePointLeft(2);
    }

    private String normalize(String text) {
        return text
                .toLowerCase();
    }

}
