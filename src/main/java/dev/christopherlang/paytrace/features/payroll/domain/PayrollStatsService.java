package dev.christopherlang.paytrace.features.payroll.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollStats.MonthlyStats;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStats.YearlyStats;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollStatsService {

    private final PayrollRepository payrollRepository;

    public List<PayrollStats> calculateStats(String userId, List<PayrollStatsQuery> queries) {

        YearMonth start = calculateMinStart(queries);
        YearMonth end = calculateMaxEnd(queries);
        YearMonth dbStart = start != null ? start.minusMonths(12) : null;

        Set<PayrollEntryType> requiredTypes = queries.stream()
            .flatMap(q -> q.getMetric().getTypes().stream())
            .collect(Collectors.toSet());

        List<RawPayrollData> rawData = payrollRepository.findRawData(userId, dbStart, end, requiredTypes);

        return queries.stream().map(query -> buildStatsForQuery(query, rawData)).toList();
    }

    private YearMonth calculateMinStart(List<PayrollStatsQuery> queries) {
        return queries.stream()
            .map(q -> {
                if (q.getInterval() == PayrollStatsInterval.YEARLY) {
                    return q.getStartYear() != null ? q.getStartYear().atMonth(1) : null;
                }
                return q.getStartYearMonth();
            })
            .filter(Objects::nonNull)
            .min(YearMonth::compareTo)
            .orElse(null);
    }

    private YearMonth calculateMaxEnd(List<PayrollStatsQuery> queries) {
        return queries.stream()
            .map(q -> {
                if (q.getInterval() == PayrollStatsInterval.YEARLY) {
                    return q.getEndYear() != null ? q.getEndYear().atMonth(12) : null;
                }
                return q.getEndYearMonth();
            })
            .filter(Objects::nonNull)
            .max(YearMonth::compareTo)
            .orElse(null);
    }

    private boolean isWithinRange(YearMonth target, PayrollStatsQuery query) {
        YearMonth start;
        YearMonth end;

        if (query.getInterval() == PayrollStatsInterval.YEARLY) {
            start = (query.getStartYear() != null) ? query.getStartYear().atMonth(1) : null;
            end = (query.getEndYear() != null) ? query.getEndYear().atMonth(12) : null;
        } else {
            start = query.getStartYearMonth();
            end = query.getEndYearMonth();
        }

        boolean satisfyStart = (start == null) || !target.isBefore(start);
        boolean satisfyEnd = (end == null) || !target.isAfter(end);

        return satisfyStart && satisfyEnd;
    }

    private Map<YearMonth, BigDecimal> sumByMonth(PayrollStatsQuery query, List<RawPayrollData> data) {
        PayrollMetric metric = query.getMetric();

        return data.stream()
            .collect(Collectors.groupingBy(
                RawPayrollData::accountingPeriod,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    d -> metric.calculateContribution(d.type(), d.amount()),
                    BigDecimal::add
                )
            ));
    }

    private Map<YearMonth, BigDecimal> fillMissingMonths(Map<YearMonth, BigDecimal> summedByMonth, PayrollStatsQuery query) {
        YearMonth start;
        YearMonth end;

        if (query.getInterval() == PayrollStatsInterval.YEARLY) {
            start = query.getStartYear() != null ? query.getStartYear().atMonth(1) : null;
            end = query.getEndYear() != null ? query.getEndYear().atMonth(12) : null;
        } else {
            start = query.getStartYearMonth();
            end = query.getEndYearMonth();
        }

        if (start == null || end == null) {
            if (summedByMonth.isEmpty()) {
                return summedByMonth;
            }
            if (start == null) {
                start = summedByMonth.keySet().stream().min(YearMonth::compareTo).orElse(null);
            }
            if (end == null) {
                end = summedByMonth.keySet().stream().max(YearMonth::compareTo).orElse(null);
            }
            if (start == null || end == null) {
                return summedByMonth;
            }
        }

        Map<YearMonth, BigDecimal> filledMap = new LinkedHashMap<>();

        YearMonth current = start;
        while (!current.isAfter(end)) {
            filledMap.put(current, summedByMonth.getOrDefault(current, BigDecimal.ZERO));
            current = current.plusMonths(1);
        }

        return filledMap;
    }

    private Map<Year, Map<YearMonth, BigDecimal>> fillMissingYears(Map<Year, Map<YearMonth, BigDecimal>> byYear, PayrollStatsQuery query) {
        Year startYear = query.getStartYear();
        Year endYear = query.getEndYear();

        if (startYear == null || endYear == null) {
            if (byYear.isEmpty()) {
                return byYear;
            }
            if (startYear == null) {
                startYear = byYear.keySet().stream().min(Year::compareTo).orElse(null);
            }
            if (endYear == null) {
                endYear = byYear.keySet().stream().max(Year::compareTo).orElse(null);
            }
            if (startYear == null || endYear == null) {
                return byYear;
            }
        }

        Map<Year, Map<YearMonth, BigDecimal>> filledMap = new LinkedHashMap<>();

        for (Year current = startYear; !current.isAfter(endYear); current = current.plusYears(1)) {
            filledMap.put(current, byYear.getOrDefault(current, new LinkedHashMap<>()));
        }

        return filledMap;
    }

    private BigDecimal calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateYearlySum(Year year, Map<YearMonth, BigDecimal> allMonths) {
        return allMonths.entrySet().stream()
            .filter(e -> e.getKey().getYear() == year.getValue())
            .map(Map.Entry::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private YearlyStats buildYearlyStats(Year year, Map<YearMonth, BigDecimal> monthsInYear, Map<YearMonth, BigDecimal> allMonths, PayrollStatsInterval interval) {
        List<MonthlyStats> monthlyStats = interval == PayrollStatsInterval.MONTHLY ?
            buildMonthlyStats(monthsInYear, allMonths) : List.of();

        BigDecimal yearlySum = monthsInYear.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal yearlyAverage = monthsInYear.isEmpty() ? BigDecimal.ZERO :
                yearlySum.divide(BigDecimal.valueOf(monthsInYear.size()), 2, RoundingMode.HALF_UP);

        return YearlyStats.builder()
            .year(year)
            .yearlySum(yearlySum)
            .yearlyAverage(yearlyAverage)
            .monthlyBreakdown(monthlyStats)
            .yearOverYearGrowth(calculateGrowth(yearlySum, calculateYearlySum(year.minusYears(1), allMonths)))
            .build();
    }

    private List<MonthlyStats> buildMonthlyStats(Map<YearMonth, BigDecimal> monthsInYear, Map<YearMonth, BigDecimal> allMonths) {
        BigDecimal[] cumulativeSum = {BigDecimal.ZERO};

        return monthsInYear.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                BigDecimal currentValue = entry.getValue();
                cumulativeSum[0] = cumulativeSum[0].add(currentValue);

                return MonthlyStats.builder()
                    .yearMonth(entry.getKey())
                    .value(currentValue)
                    .monthOverMonthGrowth(calculateGrowth(currentValue, allMonths.get(entry.getKey().minusMonths(1))))
                    .yearOverYearGrowth(calculateGrowth(currentValue, allMonths.get(entry.getKey().minusYears(1))))
                    .build();
            })
            .toList();
    }

    private List<YearlyStats> addCumulativeSumToYears(List<YearlyStats> yearlyStats) {
        BigDecimal[] cumulativeSum = {BigDecimal.ZERO};

        return yearlyStats.stream()
            .map(stat -> {
                cumulativeSum[0] = cumulativeSum[0].add(stat.getYearlySum());

                return PayrollStats.YearlyStats.builder()
                    .year(stat.getYear())
                    .yearlySum(stat.getYearlySum())
                    .yearlyAverage(stat.getYearlyAverage())
                    .yearlyCumulativeSum(cumulativeSum[0])
                    .yearOverYearGrowth(stat.getYearOverYearGrowth())
                    .monthlyBreakdown(stat.getMonthlyBreakdown())
                    .build();
            })
            .toList();
    }

    private PayrollStats buildStatsForQuery(PayrollStatsQuery query, List<RawPayrollData> data) {
        List<PayrollEntryType> targetTypes = query.getMetric().getTypes();

        List<RawPayrollData> filtered = data.stream()
                .filter(d -> isWithinRange(d.accountingPeriod(), query))
                .filter(d -> targetTypes.contains(d.type()))
                .toList();

        List<RawPayrollData> allDataByType = data.stream()
                .filter(d -> targetTypes.contains(d.type()))
                .toList();

        Map<YearMonth, BigDecimal> summedByMonth = sumByMonth(query, filtered);
        Map<YearMonth, BigDecimal> summedByMonthAllData = sumByMonth(query, allDataByType);

        Map<YearMonth, BigDecimal> filledByMonth = query.getInterval() == PayrollStatsInterval.MONTHLY ?
            fillMissingMonths(summedByMonth, query) : summedByMonth;

        Map<Year, Map<YearMonth, BigDecimal>> byYear = filledByMonth.entrySet().stream()
            .collect(Collectors.groupingBy(
                entry -> Year.of(entry.getKey().getYear()),
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
            ));

        Map<Year, Map<YearMonth, BigDecimal>> filledByYear = query.getInterval() == PayrollStatsInterval.YEARLY ?
            fillMissingYears(byYear, query) : byYear;

        List<YearlyStats> yearlyStatsWithoutCumulative = filledByYear.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> buildYearlyStats(entry.getKey(), entry.getValue(), summedByMonthAllData, query.getInterval()))
            .toList();

        List<YearlyStats> yearlyBreakdown = addCumulativeSumToYears(yearlyStatsWithoutCumulative);

        BigDecimal totalSum = filledByMonth.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int divisor = query.getInterval() == PayrollStatsInterval.YEARLY ?
            filledByYear.size() : filledByMonth.size();

        BigDecimal overallAverage = divisor == 0 ? BigDecimal.ZERO :
            totalSum.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);

        return PayrollStats.builder()
            .metric(query.getMetric())
            .interval(query.getInterval())
            .totalSum(totalSum)
            .overallAverage(overallAverage)
            .yearlyBreakdown(yearlyBreakdown)
            .build();
    }

}
