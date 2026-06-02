package dev.christopherlang.paytrace.features.demo.domain;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollService;
import dev.christopherlang.paytrace.features.user.domain.User;
import dev.christopherlang.paytrace.features.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoResetService {

    private final UserService userService;
    private final PayrollService payrollService;
    private final SampleDataService sampleDataService;

    @Scheduled(cron = "0 0 3 * * ?", zone = "Europe/Berlin")
    @Transactional
    public void resetDemoUsersData() {
        log.info("Starting demo data reset job...");

        List<User> demoUsers = userService.getDemoUsers();
        log.info("Found {} demo users to reset.", demoUsers.size());

        for (User user : demoUsers) {
            try {
                payrollService.deleteByUserId(user.userId());

                sampleDataService.generateSampleData(user.userId());

                log.info("Reset successful for user {}", user.userId());
            } catch (Exception e) {
                log.error("Reset failed for user {}: {}", user.userId(), e.getMessage(), e);
            }
        }

        log.info("Demo data reset job completed.");
    }
}
