package com.tyclick.chroniquesdetyrieback.auth.mapper;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "biography", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "lastPasswordChangeAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(RegisterRequest request);

}