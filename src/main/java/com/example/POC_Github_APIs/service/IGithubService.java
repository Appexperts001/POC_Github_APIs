package com.example.POC_Github_APIs.service;

import com.example.POC_Github_APIs.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IGithubService {
    ResponseEntity getAccessToken(String code, String state);

    ResponseEntity<Void> githubLoginRequest();

    ResponseEntity orgUseListOfRepositories(RepositoryRequest repositoryRequestDto);

    List<BranchResponse> orgAndRepoUseListOfBranches(BranchRequest branchRequest);


    List<BranchFoldersResponse>  branchGetAllFolders(BranchFolderRequest branchFolderRequest);

    ResponseEntity cloneTheBranchProject(CloneBranchRequest cloneBranchRequest);

    ResponseEntity NewBranchAndPushProject(PushNewBranchRequestModel pushNewBranchRequestModel)throws  Exception;
}
