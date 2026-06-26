package com.tyclick.chroniquesdetyrieback.user.mapper;

import com.tyclick.chroniquesdetyrieback.user.dto.request.UpdateProfileRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    @Mapping(target = "avatarUrl", ignore = true)
    UserProfileResponse toUserProfileResponse(User user);

    /**
     * Updates the User entity with the values from the UpdateProfileRequest.
     * Only non-null values from the request will be used to update the User entity.
     * @param request The UpdateProfileRequest containing the new values for the User entity.
     * @param user The User entity to be updated.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UpdateProfileRequest request, @MappingTarget User user);
}
