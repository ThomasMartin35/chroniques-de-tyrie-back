package com.tyclick.chroniquesdetyrieback.user.mapper;

import com.tyclick.chroniquesdetyrieback.auth.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "avatarUrl", ignore = true)
    UserProfileResponse toUserProfileResponse(User user);
}
