package com.tyclick.chroniquesdetyrieback.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    // TODO: Add the email field with validation (unique and valid email format)

    @Pattern(regexp = ".*\\S.*", message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(max = 1000, message = "Biography must be less than 1000 characters")
    private String biography;
}
