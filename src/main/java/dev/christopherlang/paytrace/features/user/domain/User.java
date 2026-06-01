package dev.christopherlang.paytrace.features.user.domain;

import lombok.Builder;

@Builder
public record User(
    String userId,
    boolean isDemo
) {

    public User withDemo(boolean isDemo) {
        return User.builder()
            .userId(this.userId)
            .isDemo(isDemo)
            .build();
    }

}
