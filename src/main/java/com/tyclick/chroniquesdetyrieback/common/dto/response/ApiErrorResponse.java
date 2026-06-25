package com.tyclick.chroniquesdetyrieback.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from the JSON response
public class ApiErrorResponse {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    private Map<String, String> validationErrors;

}