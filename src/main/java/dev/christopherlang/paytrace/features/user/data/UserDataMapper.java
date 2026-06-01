package dev.christopherlang.paytrace.features.user.data;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import dev.christopherlang.paytrace.features.user.domain.User;

@Mapper(componentModel = "spring")
public abstract class UserDataMapper {

    public abstract UserEntity toEntity(User user);

    @Mapping(source = "demo", target = "isDemo")
    public abstract User toUser(UserEntity entity);

}
