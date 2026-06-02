package dev.christopherlang.paytrace.features.demo.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntry;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SampleDataService {

    private final PayrollService payrollService;

    @Transactional
    public void generateSampleData(String userId) {
        YearMonth endMonth = YearMonth.now().minusMonths(1);
        YearMonth startMonth = endMonth.minusYears(5);


        YearMonth targetInconsistentMonth = endMonth.minusYears(4).withMonth(Month.AUGUST.getValue());

        BigDecimal baseSalary = BigDecimal.valueOf(3200.00);
        BigDecimal bonusSalary = BigDecimal.valueOf(1000.00);
        YearMonth jobSwitchMonth = startMonth.plusYears(3);

        YearMonth currentMonth = startMonth;

        while (!currentMonth.isAfter(endMonth)) {
            if (currentMonth.getMonth() == Month.JANUARY && !currentMonth.equals(startMonth)) {
                baseSalary = baseSalary.multiply(BigDecimal.valueOf(1.035)).setScale(2, RoundingMode.HALF_UP);
                bonusSalary = bonusSalary.multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP);
            }

            if (currentMonth.equals(jobSwitchMonth)) {
                baseSalary = baseSalary.multiply(BigDecimal.valueOf(1.20)).setScale(2, RoundingMode.HALF_UP);
                bonusSalary = bonusSalary.multiply(BigDecimal.valueOf(1.15)).setScale(2, RoundingMode.HALF_UP);
            }

            boolean isNovember = (currentMonth.getMonth() == Month.NOVEMBER);
            BigDecimal activeBonus = isNovember ? bonusSalary : BigDecimal.ZERO;

            List<PayrollEntry> entries = createConsistentEntries(baseSalary, activeBonus);

            BigDecimal totalGross = baseSalary.add(activeBonus);
            BigDecimal totalDeductions = entries.stream()
                .filter(e -> e.type().isGross() && e.type().isDeduction())
                .map(PayrollEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal payout = totalGross.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);

            if (currentMonth.equals(targetInconsistentMonth)) {
                payout = payout.subtract(BigDecimal.valueOf(500.00));
            }

            Payroll payroll = Payroll.builder()
                .userId(userId)
                .accountingPeriod(currentMonth)
                .payout(payout)
                .entries(entries)
                .build();

            payrollService.create(payroll);

            currentMonth = currentMonth.plusMonths(1);
        }
    }

    private List<PayrollEntry> createConsistentEntries(BigDecimal base, BigDecimal bonus) {
        List<PayrollEntry> entries = new ArrayList<>();

        entries.add(createEntry(PayrollEntryType.BASE_SALARY, base));
        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            entries.add(createEntry(PayrollEntryType.BONUS_SALARY, bonus));
        }

        BigDecimal taxRateBase = BigDecimal.valueOf(0.14);
        BigDecimal taxRateBonus = BigDecimal.valueOf(0.22);

        BigDecimal incomeTaxBase = base.multiply(taxRateBase).setScale(2, RoundingMode.HALF_UP);
        entries.add(createEntry(PayrollEntryType.BASE_INCOME_TAX, incomeTaxBase));

        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal incomeTaxBonus = bonus.multiply(taxRateBonus).setScale(2, RoundingMode.HALF_UP);
            entries.add(createEntry(PayrollEntryType.BONUS_INCOME_TAX, incomeTaxBonus));

            entries.add(createEntry(PayrollEntryType.BONUS_CHURCH_TAX, incomeTaxBonus.multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP)));
            entries.add(createEntry(PayrollEntryType.BONUS_SOLIDARITY_SURCHARGE, incomeTaxBonus.multiply(BigDecimal.valueOf(0.055)).setScale(2, RoundingMode.HALF_UP)));
        }

        entries.add(createEntry(PayrollEntryType.BASE_CHURCH_TAX, incomeTaxBase.multiply(BigDecimal.valueOf(0.09)).setScale(2, RoundingMode.HALF_UP)));
        entries.add(createEntry(PayrollEntryType.BASE_SOLIDARITY_SURCHARGE, incomeTaxBase.multiply(BigDecimal.valueOf(0.055)).setScale(2, RoundingMode.HALF_UP)));

        entries.add(createSocialEntry(PayrollEntryType.BASE_HEALTH_INSURANCE, base, 0.081));
        entries.add(createSocialEntry(PayrollEntryType.BASE_PENSION_INSURANCE, base, 0.093));
        entries.add(createSocialEntry(PayrollEntryType.BASE_UNEMPLOYMENT_INSURANCE, base, 0.013));
        entries.add(createSocialEntry(PayrollEntryType.BASE_LONG_TERM_CARE_INSURANCE, base, 0.023));

        if (bonus.compareTo(BigDecimal.ZERO) > 0) {
            entries.add(createSocialEntry(PayrollEntryType.BONUS_HEALTH_INSURANCE, bonus, 0.081));
            entries.add(createSocialEntry(PayrollEntryType.BONUS_PENSION_INSURANCE, bonus, 0.093));
            entries.add(createSocialEntry(PayrollEntryType.BONUS_UNEMPLOYMENT_INSURANCE, bonus, 0.013));
            entries.add(createSocialEntry(PayrollEntryType.BONUS_LONG_TERM_CARE_INSURANCE, bonus, 0.023));
        }

        entries.add(createEntry(PayrollEntryType.REIMBURSEMENTS, BigDecimal.ZERO.setScale(2)));
        entries.add(createEntry(PayrollEntryType.DISBURSEMENTS, BigDecimal.ZERO.setScale(2)));

        return entries;
    }

    private PayrollEntry createSocialEntry(PayrollEntryType type, BigDecimal amount, double rate) {
        return createEntry(type, amount.multiply(BigDecimal.valueOf(rate)).setScale(2, RoundingMode.HALF_UP));
    }

    private PayrollEntry createEntry(PayrollEntryType type, BigDecimal amount) {
        return PayrollEntry.builder()
            .type(type)
            .amount(amount)
            .build();
    }
}
