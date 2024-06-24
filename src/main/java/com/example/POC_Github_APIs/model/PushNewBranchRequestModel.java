package com.example.POC_Github_APIs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushNewBranchRequestModel {
    @NotBlank
    private String accessToken;
    @NotBlank
    private String repoUrl;
    @NotBlank
    private String repoPath;
    @NotBlank
    private String newBranchName;
}
