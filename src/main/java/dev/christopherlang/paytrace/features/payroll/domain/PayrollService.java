package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.christopherlang.paytrace.common.BusinessException;
import dev.christopherlang.paytrace.common.ErrorCode;
import dev.christopherlang.paytrace.common.UserContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserContext userContext;

    @Transactional
    public void create(Payroll payroll) {
        if (payroll.hasDuplicateEntryTypes()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYROLL_ENTRY);
        }

        String userId = userContext.getUserId();
        if (payrollRepository.existsByAccountingPeriod(userId, payroll.accountingPeriod())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNTING_PERIOD);
        }

        BigDecimal consistencyDeviation = calculateConsistencyDeviation(payroll.payout(), payroll.entries());
        Payroll payrollWithDeviation = Payroll.builder()
            .payrollId(payroll.payrollId())
            .userId(userId)
            .accountingPeriod(payroll.accountingPeriod())
            .payout(payroll.payout())
            .consistencyDeviation(consistencyDeviation)
            .entries(payroll.entries())
            .build();

        payrollRepository.save(payrollWithDeviation);
    }

    @Transactional
    public void update(Payroll payroll) {
        if (payroll.hasDuplicateEntryTypes()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYROLL_ENTRY);
        }

        String userId = userContext.getUserId();
        Payroll existing = getById(payroll.payrollId());
        BigDecimal consistencyDeviation = calculateConsistencyDeviation(payroll.payout(), payroll.entries());

        Payroll merged = Payroll.builder()
            .payrollId(payroll.payrollId())
            .userId(userId)
            .accountingPeriod(existing.accountingPeriod())
            .payout(payroll.payout())
            .consistencyDeviation(consistencyDeviation)
            .entries(payroll.entries())
            .build();

        payrollRepository.save(merged);
    }

    @Transactional
    public Payroll getById(UUID payrollId) {
        String userId = userContext.getUserId();
        return payrollRepository.findById(userId, payrollId);
    }

    @Transactional
    public PayrollGroups getMultiple(PayrollSearchCriteria criteria) {
        String userId = userContext.getUserId();
        return payrollRepository.findGrouped(userId, criteria);
    }

    @Transactional(readOnly = true)
    public boolean hasInconsistentPayrolls() {
        String userId = userContext.getUserId();
        return payrollRepository.hasInconsistentPayrolls(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsAccountingPeriod(java.time.YearMonth accountingPeriod) {
        String userId = userContext.getUserId();
        return payrollRepository.existsByAccountingPeriod(userId, accountingPeriod);
    }

    @Transactional
    public void delete(UUID payrollId) {
        String userId = userContext.getUserId();
        payrollRepository.deleteById(userId, payrollId);
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
