package com.example.POC_Github_APIs.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class CloneBranchRequest {

    @NotBlank
    private String accessToken;
    @NotBlank
    private String repoUrl;
    @NotBlank
    private String repoName;
    @NotBlank
    private String cloneBranchName;


}
