package com.tyclick.chroniquesdetyrieback.auth.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;

    private String tokenType;

}

