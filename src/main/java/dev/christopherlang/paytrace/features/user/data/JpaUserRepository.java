package dev.christopherlang.paytrace.features.user.data;

import org.springframework.data.jpa.repository.JpaRepository;


public interface JpaUserRepository extends JpaRepository<UserEntity, String> {}

