package dev.christopherlang.paytrace.features.user.data;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import dev.christopherlang.paytrace.features.user.domain.User;
import dev.christopherlang.paytrace.features.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpaRepo;
    private final UserDataMapper mapper;

    @Override
    public void save(User user) {
        UserEntity entity = mapper.toEntity(user);
        jpaRepo.save(entity);
    }

    @Override
    public Optional<User> findById(String userId) {
        return jpaRepo.findById(userId)
            .map(mapper::toUser);
    }

}
