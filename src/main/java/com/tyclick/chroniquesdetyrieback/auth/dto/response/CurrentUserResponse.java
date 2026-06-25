package com.tyclick.chroniquesdetyrieback.auth.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserResponse {

    private UUID id;

    private String username;

    private String email;

    private String role;
}