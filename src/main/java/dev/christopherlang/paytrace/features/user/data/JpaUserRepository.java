package dev.christopherlang.paytrace.features.user.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface JpaUserRepository extends JpaRepository<UserEntity, String> {

    List<UserEntity> findByIsDemo(boolean demo);

}

