package dev.christopherlang.paytrace.features.payroll.api;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.SubclassMapping;

import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollGroups;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollSearchCriteria;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStats;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsQuery;

@Mapper(componentModel = "spring")
public interface PayrollApiMapper {

    @Mapping(source = "limit", target = "yearLimit")
    @Mapping(source = "cursor", target = "beforeYear")
    PayrollSearchCriteria toCriteria(GetPayrollsRequest request);

    @Mapping(source = "lowestYearAvailable", target = "nextCursor")
    @Mapping(source = "groups", target = "years")
    GetPayrollsResponse toResponse(PayrollGroups groups);

    GetPayrollsResponse.PayrollGroupDto toYearGroupDto(PayrollGroups.PayrollGroup payrollGroup);

    GetPayrollsResponse.PayrollGroupDto.PayrollSummaryDto toSummaryDto(PayrollGroups.PayrollGroup.PayrollSummary payrollSummary);

    @Mapping(target = "payrollId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "consistencyDeviation", ignore = true)
    Payroll toPayroll(CreatePayrollRequest request);

    @Mapping(source = "payrollId", target = "payrollId")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "accountingPeriod", ignore = true)
    @Mapping(target = "consistencyDeviation", ignore = true)
    Payroll toPayroll(UpdatePayrollRequest request);

    GetPayrollResponse toPayrollResponse(Payroll payroll);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @SubclassMapping(source = MonthlyPayrollStatsRequest.class, target = PayrollStatsQuery.class)
    @SubclassMapping(source = YearlyPayrollStatsRequest.class, target = PayrollStatsQuery.class)
    PayrollStatsQuery toStatsQuery(PayrollStatsRequest request);

    @Mapping(source = "totalSum", target = "sum")
    @Mapping(source = "overallAverage", target = "average")
    @Mapping(source = "yearlyBreakdown", target = "years")
    PayrollStatsResponse toStatsResponse(PayrollStats stats);

    @Mapping(source = "yearlySum", target = "sum")
    @Mapping(source = "yearlyCumulativeSum", target = "cumulativeSum")
    @Mapping(source = "yearlyAverage", target = "average")
    @Mapping(source = "yearOverYearGrowth", target = "yoyPercentage")
    @Mapping(source = "monthlyBreakdown", target = "months")
    PayrollStatsResponse.YearlyStatsResponse toYearlyResponse(PayrollStats.YearlyStats yearlyStats);

    @Mapping(source = "yearOverYearGrowth", target = "yoyPercentage")
    @Mapping(source = "monthOverMonthGrowth", target = "momPercentage")
    PayrollStatsResponse.MonthlyStatsResponse toMonthlyResponse(PayrollStats.MonthlyStats monthlyStats);

}
