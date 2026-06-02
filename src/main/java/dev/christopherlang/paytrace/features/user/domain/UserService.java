package dev.christopherlang.paytrace.features.user.domain;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getDemoUsers() {
        return userRepository.findByIsDemo(true);
    }

}
