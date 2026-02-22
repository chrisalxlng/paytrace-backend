package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntry;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;


@Component
public class OptaDataPayrollParserProvider implements PayrollParser {

    private static final Set<String> PROVIDER_KEYWORDS = Set.of("entgeltabrechnung", "persönliche / organisatorische daten");

    private static final DateTimeFormatter GERMAN_MONTH_YEAR_FORMATTER =
        new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM yyyy")
                .toFormatter(Locale.GERMAN);

    @Override
    public boolean supports(String pdfText) {
        return PROVIDER_KEYWORDS.stream().allMatch(keyword -> normalize(pdfText).contains(keyword));
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

        BigDecimal reimbursements = BigDecimal.ZERO;
        BigDecimal disbursements = BigDecimal.ZERO;

        BigDecimal payout = BigDecimal.ZERO;
        YearMonth accountingPeriod = null;

        boolean inEarningsSection = false;
        boolean inDeductionsSection = false;
        boolean inBursementsSection = false;
        boolean inPaymentsSection = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("basisbezüge")) {
                inEarningsSection = true;
            }
            if (line.startsWith("gesetzliche abzüge")) {
                inEarningsSection = false;
                inDeductionsSection = true;
            }
            if (line.startsWith("be- und abzüge")) {
                inDeductionsSection = false;
                inBursementsSection = true;
            }
            if (line.startsWith("zahlungen")) {
                inBursementsSection = false;
                inPaymentsSection = true;
            }

            if (line.startsWith("im")) {
                String cleaned = line.replaceFirst("^\\s*im\\s+", "");
                accountingPeriod = YearMonth.parse(cleaned, GERMAN_MONTH_YEAR_FORMATTER);
            }

            if (inEarningsSection) {
                Matcher salaryMatcher = Pattern.compile(
                    "\\blsg\\b"
                ).matcher(line);

                Matcher bonusMatcher = Pattern.compile(
                    "\\belsg\\b"
                ).matcher(line);

                if (salaryMatcher.find()) {
                    grossSalaryBase = grossSalaryBase.add(parseAmount(line));
                }

                if (bonusMatcher.find()) {
                    grossSalaryBonus = grossSalaryBonus.add(parseAmount(line));
                }
            }

            if (inDeductionsSection) {
                if (line.startsWith("lohnsteuer, lfd.")) {
                    incomeTaxBase = parseAmountLine(line);
                }
                if (line.startsWith("lohnsteuer, ez")) {
                    incomeTaxBonus = parseAmountLine(line);
                }
                if (line.startsWith("krankenversicherung, lfd.")) {
                    healthInsuranceBase = parseAmountLine(line);
                }
                if (line.startsWith("krankenversicherung, ez")) {
                    healthInsuranceBonus = parseAmountLine(line);
                }
                if (line.startsWith("rentenversicherung, lfd.")) {
                    pensionInsuranceBase = parseAmountLine(line);
                }
                if (line.startsWith("rentenversicherung, ez")) {
                    pensionInsuranceBonus = parseAmountLine(line);
                }
                if (line.startsWith("arbeitslosenvers., lfd.")) {
                    unemploymentInsuranceBase = parseAmountLine(line);
                }
                if (line.startsWith("arbeitslosenvers., ez")) {
                    unemploymentInsuranceBonus = parseAmountLine(line);
                }
                if (line.startsWith("pflegeversicherung, lfd.")) {
                    longTermCareInsuranceBase = parseAmountLine(line);
                }
                if (line.startsWith("pflegeversicherung, ez")) {
                    longTermCareInsuranceBonus = parseAmountLine(line);
                }
                if (line.startsWith("kirchensteuer, lfd.")) {
                    churchTaxBase = parseAmountLine(line);
                }
                if (line.startsWith("kirchensteuer, ez")) {
                    churchTaxBonus = parseAmountLine(line);
                }
                if (line.startsWith("solidaritätszuschlag, lfd.")) {
                    solidaritySurchargeBase = parseAmountLine(line);
                }
                if (line.startsWith("solidaritätszuschlag, ez")) {
                    solidaritySurchargeBonus = parseAmountLine(line);
                }
            }

            if (inBursementsSection) {
                BigDecimal bursement = parseAmount(line);
                if (bursement.signum() > 0) {
                    reimbursements = reimbursements.add(bursement);
                } else if (bursement.signum() < 0) {
                    disbursements = disbursements.add(bursement.abs());
                }
            }

            if (inPaymentsSection) {
                if (line.startsWith("überweisung")) {
                    payout = parseAmount(line);
                }
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
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.REIMBURSEMENTS)
                        .amount(reimbursements)
                        .build(),
                    PayrollEntry
                        .builder()
                        .type(PayrollEntryType.DISBURSEMENTS)
                        .amount(disbursements)
                        .build()
                    )
                )
                .accountingPeriod(accountingPeriod)
                .payout(payout)
                .build();
    }

    private BigDecimal parseAmountLine(String text) {
        String[] parts = text.strip().split("\\s+");
        if (parts.length <= 3) {
            return BigDecimal.ZERO;
        }

        String middleValue = parts[2];
        return parseAmount(middleValue);
    }

    private BigDecimal parseAmount(String text) {
        Matcher matcher = Pattern.compile(
            "\\d+(\\.\\d{3})*,\\d{2}(\\s*-)?"
        ).matcher(text);

        if (matcher.find()) {
            String value = matcher.group();

            boolean negative = value.contains("-");

            value = value
                .replace("-", "")
                .replace(".", "")
                .replace(",", ".")
                .trim();

            BigDecimal result = new BigDecimal(value);

            return negative ? result.negate() : result;
        }

        return BigDecimal.ZERO;
    }

    private String normalize(String text) {
        return text
                .replace("\r", "")
                .trim()
                .toLowerCase();
    }

}
