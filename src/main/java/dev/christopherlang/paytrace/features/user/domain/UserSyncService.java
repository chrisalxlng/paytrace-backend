package dev.christopherlang.paytrace.features.user.domain;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;

    @Async
    public void syncUser(Jwt jwt) {
        executeSync(jwt);
    }

    @Transactional
    protected void executeSync(Jwt jwt) {
        String userId = jwt.getSubject();
        boolean isDemo = Boolean.TRUE.equals(jwt.getClaimAsBoolean("is_demo"));

        User user = userRepository.findById(userId)
            .orElseGet(() -> User.builder().userId(userId).build());

        User updatedUser = user.withDemo(isDemo);

        userRepository.save(updatedUser);
    }

}
