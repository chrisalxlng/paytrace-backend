package dev.christopherlang.paytrace.features.payroll.data;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntry;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollGroups;

@Mapper(componentModel = "spring")
public abstract class PayrollDataMapper {

    @Mapping(source = "payrollId", target = "payrollId")
    @Mapping(target = "consistencyDeviation", expression = "java(calculateConsistencyDeviation(payroll.entries(), payroll.payout()))")
    public abstract PayrollEntity toEntity(Payroll payroll);

    @Mapping(target = "payrollEntryId", ignore = true)
    @Mapping(target = "payroll", ignore = true)
    public abstract PayrollEntryEntity toEntryEntity(PayrollEntry entry);

    @AfterMapping
    protected void linkEntries(@MappingTarget PayrollEntity entity) {
        if (entity.getEntries() != null) {
            entity.getEntries().forEach(entry -> entry.setPayroll(entity));
        }
    }

    public PayrollGroups toPayrollGroups(List<Integer> yearValues, List<PayrollEntity> entities, boolean hasMore) {
        Map<Integer, List<PayrollEntity>> groupedByYear = entities.stream()
            .collect(Collectors.groupingBy(e -> e.getAccountingPeriod().getYear()));

        List<PayrollGroups.PayrollGroup> yearGroups = yearValues.stream()
            .map(year -> {
                List<PayrollEntity> yearEntities = groupedByYear.getOrDefault(year, List.of());

                return PayrollGroups.PayrollGroup.builder()
                    .year(Year.of(year))
                    .count(yearEntities.size())
                    .sum(calculateTotalPayout(yearEntities))
                    .entries(toSummaryList(yearEntities))
                    .build();
            })
            .filter(group -> group.getCount() > 0)
            .toList();

        return PayrollGroups.builder()
            .groups(yearGroups)
            .hasMore(hasMore)
            .lowestYearAvailable(hasMore ? Year.of(yearValues.get(yearValues.size() - 1)) : null)
            .build();
    }

    protected BigDecimal calculateTotalPayout(List<PayrollEntity> entities) {
        return entities.stream()
            .map(PayrollEntity::getPayout)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public abstract List<PayrollGroups.PayrollGroup.PayrollSummary> toSummaryList(List<PayrollEntity> entities);

    @Mapping(source = "payrollId", target = "payrollId")
    @Mapping(source = "accountingPeriod", target = "accountingPeriod")
    @Mapping(source = "consistencyDeviation", target = "consistencyDeviation")
    @Mapping(source = "payout", target = "payout")
    public abstract PayrollGroups.PayrollGroup.PayrollSummary toSummary(PayrollEntity entity);

    public abstract Payroll toPayroll(PayrollEntity entity);

    protected BigDecimal calculateConsistencyDeviation(List<PayrollEntry> entries, BigDecimal payout) {
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
