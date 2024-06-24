package com.example.POC_Github_APIs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties (ignoreUnknown = true)
public class BranchFoldersResponse {
    private int key;
    private String label;
    private String path;
    private List<BranchFoldersResponse> children;
}