package com.example.POC_Github_APIs.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;


@Data
@AllArgsConstructor
@Builder
@Validated
public class RepositoryRequest {

    @NotBlank
    private  String accessToken;
    @NotBlank
    private String orgName;
    @NotBlank
    private String githubState;
}

