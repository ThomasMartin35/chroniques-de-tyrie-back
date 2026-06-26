package com.tyclick.chroniquesdetyrieback.user.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private UUID id;

    private String username;

    private String email;

    private String role;

    private String biography;

    private String avatarUrl;

    private Boolean isActive;
}