package dev.christopherlang.paytrace.features.payroll.data;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;
import dev.christopherlang.paytrace.features.payroll.domain.RawPayrollData;

public interface JpaPayrollRepository extends JpaRepository<PayrollEntity, UUID> {
    boolean existsByUserIdAndAccountingPeriod(String userId, YearMonth accountingPeriod);

    @Query("""
        SELECT new dev.christopherlang.paytrace.features.payroll.domain.RawPayrollData(
            e.payroll.accountingPeriod,
            e.type,
            e.amount
        )
        FROM PayrollEntryEntity e
        WHERE e.payroll.userId = :userId
        AND (:start IS NULL OR e.payroll.accountingPeriod >= :start)
        AND (:end IS NULL OR e.payroll.accountingPeriod <= :end)
        AND e.type IN :types
    """)
    List<RawPayrollData> findRawData(
        @Param("userId") String userId,
        @Param("start") YearMonth start,
        @Param("end") YearMonth end,
        @Param("types") Collection<PayrollEntryType> types
    );

    @Query(value = """
        SELECT DISTINCT CAST(SUBSTRING(accounting_period, 1, 4) AS INTEGER) as year_val
        FROM payrolls
        WHERE user_id = :userId
        AND (:beforeYear IS NULL OR CAST(SUBSTRING(accounting_period, 1, 4) AS INTEGER) < :beforeYear)
        AND (:consistent IS NULL OR (CASE WHEN :consistent = true THEN consistency_deviation = 0 ELSE consistency_deviation > 0 END))
        ORDER BY year_val DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Integer> findNextAvailableYears(
        @Param("userId") String userId,
        @Param("beforeYear") Integer beforeYear,
        @Param("limit") int limit,
        @Param("consistent") Boolean consistent
    );

    @Query(value = """
        SELECT * FROM payrolls
        WHERE user_id = :userId
        AND SUBSTRING(accounting_period, 1, 4) IN :years
        AND (:consistent IS NULL OR (CASE WHEN :consistent = true THEN consistency_deviation = 0 ELSE consistency_deviation > 0 END))
        ORDER BY accounting_period DESC
        """, nativeQuery = true)
    List<PayrollEntity> findAllByYears(
        @Param("userId") String userId,
        @Param("years") List<String> years,
        @Param("consistent") Boolean consistent
    );

    @Query(value = """
        SELECT EXISTS(
            SELECT 1 FROM payrolls WHERE user_id = :userId AND consistency_deviation > 0
        )
        """, nativeQuery = true)
    boolean hasInconsistentPayrolls(@Param("userId") String userId);

    void deleteByUserId(String userId);

}
