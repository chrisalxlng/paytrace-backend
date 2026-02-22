package dev.christopherlang.paytrace.features.payroll.api;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntry;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;

@Component
public class SagePayrollParserProvider implements PayrollParser {

    private static final String PROVIDER_KEYWORD = "sage gmbh";

    private static final Pattern GERMAN_NUMBER_PATTERN =
            Pattern.compile("(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");

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

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.contains("abrechnungsmonat")) {
                if (i + 1 < lines.length) {
                    accountingPeriod = parseGermanMonth(lines[i + 1]);
                }
            }

            if (line.contains("sonstige bez")) {
                grossSalaryBase  = extract(lines, i, 3);
                grossSalaryBonus = extract(lines, i, 4);

                incomeTaxBase    = extract(lines, i, 5);
                incomeTaxBonus   = extract(lines, i, 6);
                churchTaxBase  = extract(lines, i, 7);
                churchTaxBonus = extract(lines, i, 8);
                solidaritySurchargeBase    = extract(lines, i, 9);
                solidaritySurchargeBonus   = extract(lines, i, 10);

                healthInsuranceBase  = extract(lines, i, 14);
                healthInsuranceBonus = extract(lines, i, 15);
                pensionInsuranceBase    = extract(lines, i, 16);
                pensionInsuranceBonus   = extract(lines, i, 17);
                unemploymentInsuranceBase  = extract(lines, i, 18);
                unemploymentInsuranceBonus = extract(lines, i, 19);
                longTermCareInsuranceBase    = extract(lines, i, 20);
                longTermCareInsuranceBonus   = extract(lines, i, 21);
            }

            if (line.contains("auszahlung")) {
                payout = extractFromLine(line);
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

    private BigDecimal extract(String[] lines, int baseIndex, int offset) {
        int targetIndex = baseIndex + offset;

        if (targetIndex >= lines.length) {
            return BigDecimal.ZERO;
        }

        return extractFromLine(lines[targetIndex]);
    }

    private BigDecimal extractFromLine(String line) {
        Matcher matcher = GERMAN_NUMBER_PATTERN.matcher(line);

        if (matcher.find()) {
            return parseGermanDecimal(matcher.group(1));
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal parseGermanDecimal(String value) {
        return new BigDecimal(
                value.replace(".", "")
                     .replace(",", ".")
        );
    }

    private static final Map<String, Integer> MONTH_MAP = Map.ofEntries(
        Map.entry("jan", 1),
        Map.entry("feb", 2),
        Map.entry("mrz", 3),
        Map.entry("apr", 4),
        Map.entry("mai", 5),
        Map.entry("jun", 6),
        Map.entry("jul", 7),
        Map.entry("aug", 8),
        Map.entry("sep", 9),
        Map.entry("okt", 10),
        Map.entry("nov", 11),
        Map.entry("dez", 12)
    );

    private YearMonth parseGermanMonth(String value) {
        String normalized = value
                .toLowerCase()
                .replace(".", "")
                .trim();

        String[] parts = normalized.split("\\s+");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Accounting period could not be parsed: " + value);
        }

        Integer month = MONTH_MAP.get(parts[0]);
        int year = Integer.parseInt(parts[1]);

        if (month == null) {
            throw new IllegalArgumentException("Unknown month: " + parts[0]);
        }

        return YearMonth.of(year, month);
    }

    private String normalize(String text) {
        return text
                .replace("\r", "")
                .trim()
                .toLowerCase();
    }

}
