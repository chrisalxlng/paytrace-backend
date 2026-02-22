package dev.christopherlang.paytrace.features.payroll.data;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import dev.christopherlang.paytrace.common.BusinessException;
import dev.christopherlang.paytrace.common.ErrorCode;
import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollGroups;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollRepository;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollSearchCriteria;
import dev.christopherlang.paytrace.features.payroll.domain.RawPayrollData;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PayrollRepositoryImpl implements PayrollRepository {

    private final JpaPayrollRepository jpaRepo;
    private final PayrollDataMapper mapper;

    @Override
    public boolean existsByAccountingPeriod(String userId, YearMonth accountingPeriod) {
        return jpaRepo.existsByUserIdAndAccountingPeriod(userId, accountingPeriod);
    }

    @Override
    public void save(Payroll payroll) {
        PayrollEntity entity = mapper.toEntity(payroll);
        jpaRepo.save(entity);
    }

    @Override
    public List<RawPayrollData> findRawData(
        String userId,
        YearMonth start,
        YearMonth end,
        Collection<PayrollEntryType> types
    ) {
        return jpaRepo.findRawData(userId, start, end, types);
    }

    @Override
    public PayrollGroups findGrouped(String userId, PayrollSearchCriteria criteria) {
        Integer beforeYearValue = criteria.getBeforeYear() != null ? criteria.getBeforeYear().getValue() : null;

        List<Integer> yearValues = jpaRepo.findNextAvailableYears(userId, beforeYearValue, criteria.getYearLimit(), criteria.getConsistent());
        if (yearValues.isEmpty()) {
            return PayrollGroups.builder().groups(List.of()).hasMore(false).build();
        }

        List<String> yearStrings = yearValues.stream().map(Object::toString).toList();
        List<PayrollEntity> entities = jpaRepo.findAllByYears(userId, yearStrings, criteria.getConsistent());

        Integer lowestYear = yearValues.get(yearValues.size() - 1);
        boolean hasMore = !jpaRepo.findNextAvailableYears(userId, lowestYear, 1, criteria.getConsistent()).isEmpty();

        return mapper.toPayrollGroups(yearValues, entities, hasMore);
    }

    @Override
    public Payroll findById(String userId, UUID payrollId) {
        PayrollEntity payrollEntity = jpaRepo.findById(payrollId)
            .filter(p -> p.getUserId().equals(userId))
            .orElseThrow(() -> new BusinessException(
                ErrorCode.NOT_FOUND
            ));
        return mapper.toPayroll(payrollEntity);
    }

    @Override
    public void deleteById(String userId, UUID payrollId) {
        PayrollEntity payrollEntity = jpaRepo.findById(payrollId)
            .filter(p -> p.getUserId().equals(userId))
            .orElseThrow(() -> new BusinessException(
                ErrorCode.NOT_FOUND
            ));
        jpaRepo.delete(payrollEntity);
    }

    @Override
    public boolean hasInconsistentPayrolls(String userId) {
        return jpaRepo.hasInconsistentPayrolls(userId);
    }

}
