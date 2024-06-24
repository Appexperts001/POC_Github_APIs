package com.example.POC_Github_APIs.controller;

import com.example.POC_Github_APIs.model.*;
import com.example.POC_Github_APIs.service.IGithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/automarx/githubapis")
@Validated
public class githubController {

    private final IGithubService iGithubService;

    @GetMapping("/githubLogin")
    @Validated
    public ResponseEntity<Void> githubLogin() {
        return iGithubService.githubLoginRequest();
    }

    @GetMapping("/get-access-token")
    @ResponseBody
    public ResponseEntity callback(@RequestParam String code, @RequestParam String state) {
        return iGithubService.getAccessToken(code, state);
    }

    @PostMapping("/org-all-repositories")
    public ResponseEntity getGitRepositories(@RequestBody @Valid RepositoryRequest repositoryRequestDto) {

        return iGithubService.orgUseListOfRepositories(repositoryRequestDto);
    }

    @PostMapping("/repo-all-branches")
    public List<BranchResponse> getGithubBranches(@RequestBody BranchRequest branchRequest) {
        return iGithubService.orgAndRepoUseListOfBranches(branchRequest);
    }

    @PostMapping("/branch-get-all-folders")
    public List<BranchFoldersResponse> branchAllFolder(@Valid @RequestBody BranchFolderRequest branchFolderRequest) {
        return iGithubService.branchGetAllFolders(branchFolderRequest);
    }

    @PostMapping("/clone-branch")
    public ResponseEntity CloneTheBranch(@Valid @RequestBody CloneBranchRequest cloneBranchRequest){
        return iGithubService.cloneTheBranchProject(cloneBranchRequest);
    }

    @PostMapping("/create-branch-and-push")
    public ResponseEntity createNewBranch(@Valid @RequestBody PushNewBranchRequestModel pushNewBranchRequestModel)throws  Exception{
        return iGithubService.NewBranchAndPushProject(pushNewBranchRequestModel);
    }






}
