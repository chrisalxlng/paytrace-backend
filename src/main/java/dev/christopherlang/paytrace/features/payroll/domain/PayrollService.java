package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.christopherlang.paytrace.common.BusinessException;
import dev.christopherlang.paytrace.common.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;

    @Transactional
    public void create(Payroll payroll) {
        if (payroll.hasDuplicateEntryTypes()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYROLL_ENTRY);
        }

        if (payrollRepository.existsByAccountingPeriod(payroll.userId(), payroll.accountingPeriod())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNTING_PERIOD);
        }

        BigDecimal consistencyDeviation = calculateConsistencyDeviation(payroll.payout(), payroll.entries());
        Payroll payrollWithDeviation = payroll.withConsistencyDeviation(consistencyDeviation);

        payrollRepository.save(payrollWithDeviation);
    }

    @Transactional
    public void update(Payroll payroll) {
        if (payroll.hasDuplicateEntryTypes()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYROLL_ENTRY);
        }

        Payroll existing = getById(payroll.userId(), payroll.payrollId());
        BigDecimal consistencyDeviation = calculateConsistencyDeviation(payroll.payout(), payroll.entries());

        Payroll payrollWithDeviation = payroll.withConsistencyDeviation(consistencyDeviation);
        Payroll payrollWithPeriod = payrollWithDeviation.withAccountingPeriod(existing.accountingPeriod());

        payrollRepository.save(payrollWithPeriod);
    }

    @Transactional
    public Payroll getById(String userId, UUID payrollId) {
        return payrollRepository.findById(userId, payrollId);
    }

    @Transactional
    public PayrollGroups getMultiple(String userId, PayrollSearchCriteria criteria) {

        return payrollRepository.findGrouped(userId, criteria);
    }

    @Transactional(readOnly = true)
    public boolean hasInconsistentPayrolls(String userId) {
        return payrollRepository.hasInconsistentPayrolls(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsAccountingPeriod(String userId, YearMonth accountingPeriod) {
        return payrollRepository.existsByAccountingPeriod(userId, accountingPeriod);
    }

    @Transactional
    public void delete(String userId, UUID payrollId) {
        payrollRepository.deleteById(userId, payrollId);
    }

    @Transactional
    public void deleteByUserId(String userId) {
        payrollRepository.deleteByUserId(userId);
    }

    private BigDecimal calculateConsistencyDeviation(BigDecimal payout, java.util.List<PayrollEntry> entries) {
        if (entries == null || payout == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal grossIncome = entries.stream()
                .filter(entry -> entry.type().isGross() && entry.type().isIncome())
                .map(PayrollEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossDeductions = entries.stream()
                .filter(entry -> entry.type().isGross() && entry.type().isDeduction())
                .map(PayrollEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netIncome = entries.stream()
                .filter(entry -> entry.type().isNet() && entry.type().isIncome())
                .map(PayrollEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netDeductions = entries.stream()
                .filter(entry -> entry.type().isNet() && entry.type().isDeduction())
                .map(PayrollEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal calculated = grossIncome.add(netIncome)
            .subtract(grossDeductions)
            .subtract(netDeductions);

        return payout.subtract(calculated).abs();
    }

}
