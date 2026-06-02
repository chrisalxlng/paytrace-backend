package dev.christopherlang.paytrace.features.payroll.api;

import java.util.List;
import java.util.UUID;
import java.time.YearMonth;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import dev.christopherlang.paytrace.common.UserContext;
import dev.christopherlang.paytrace.features.payroll.domain.Payroll;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollGroups;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollSearchCriteria;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsService;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollService;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStats;
import dev.christopherlang.paytrace.features.payroll.domain.PayrollStatsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;



@RequiredArgsConstructor
@RestController
@RequestMapping("/payrolls")
public class PayrollController {

    private final PayrollFileProcessor payrollFileProcessor;
    private final PayrollApiMapper payrollApiMapper;
    private final PayrollService payrollService;
    private final PayrollStatsService payrollStatsService;
    private final UserContext userContext;

    @GetMapping
    public ResponseEntity<GetPayrollsResponse> getMultiple(@Valid GetPayrollsRequest request) {
        PayrollSearchCriteria criteria = payrollApiMapper.toCriteria(request);
        String userId = userContext.getUserId();
        PayrollGroups groups = payrollService.getMultiple(userId, criteria);
        GetPayrollsResponse response = payrollApiMapper.toResponse(groups);

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/{payrollId}")
    public ResponseEntity<GetPayrollResponse> getSingle(@PathVariable UUID payrollId) {
        String userId = userContext.getUserId();
        Payroll payroll = payrollService.getById(userId, payrollId);
        GetPayrollResponse response = payrollApiMapper.toPayrollResponse(payroll);

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @ModelAttribute CreatePayrollFileRequest request
    ) {
        Payroll parsedPayroll = payrollFileProcessor.processFile(request.getFile());
        String userId = userContext.getUserId();
        Payroll payrollWithUser = parsedPayroll.withUserId(userId);

        payrollService.create(payrollWithUser);

        return ResponseEntity.ok(null);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> create(@Valid @RequestBody CreatePayrollRequest request) {
        Payroll payroll = payrollApiMapper.toPayroll(request);
        String userId = userContext.getUserId();
        Payroll payrollWithUser = payroll.withUserId(userId);
        payrollService.create(payrollWithUser);

        return ResponseEntity.ok(null);
    }

    @PostMapping(path = "/update")
    public ResponseEntity<?> update(@Valid @RequestBody UpdatePayrollRequest request) {
        Payroll payroll = payrollApiMapper.toPayroll(request);
        payrollService.update(payroll);

        return ResponseEntity.ok(null);
    }

    @PostMapping(path = "/stats")
    public ResponseEntity<List<PayrollStatsResponse>> getStats(@Valid @RequestBody List<PayrollStatsRequest> requests) {
        List<PayrollStatsQuery> queries = requests.stream().map(payrollApiMapper::toStatsQuery).toList();
        String userId = userContext.getUserId();
        List<PayrollStats> stats = payrollStatsService.calculateStats(userId, queries);
        List<PayrollStatsResponse> responses = stats.stream().map(payrollApiMapper::toStatsResponse).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping(path = "/has-inconsistent")
    public ResponseEntity<Boolean> hasInconsistent() {
        String userId = userContext.getUserId();
        return ResponseEntity.ok(payrollService.hasInconsistentPayrolls(userId));
    }

    @GetMapping(path = "/exists-period")
    public ResponseEntity<Boolean> existsAccountingPeriod(@RequestParam String accountingPeriod) {
        YearMonth period = YearMonth.parse(accountingPeriod);
        String userId = userContext.getUserId();
        return ResponseEntity.ok(payrollService.existsAccountingPeriod(userId, period));
    }

    @PostMapping(path = "/delete")
    public ResponseEntity<?> delete(@Valid @RequestBody DeletePayrollRequest request) {
        String userId = userContext.getUserId();
        payrollService.delete(userId, request.getPayrollId());
        return ResponseEntity.ok(null);
    }

}
