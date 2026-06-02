package dev.christopherlang.paytrace.features.payroll.domain;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PayrollRepository {

    boolean existsByAccountingPeriod(String userId, YearMonth accountingPeriod);

    void save(Payroll payroll);

    List<RawPayrollData> findRawData(String userId, YearMonth start, YearMonth end, Collection<PayrollEntryType> types);

    PayrollGroups findGrouped(String userId, PayrollSearchCriteria criteria);

    Payroll findById(String userId, UUID payrollId);

    void deleteById(String userId, UUID payrollId);

    void deleteByUserId(String userId);

    boolean hasInconsistentPayrolls(String userId);

}
