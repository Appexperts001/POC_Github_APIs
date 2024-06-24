package com.example.POC_Github_APIs.service.impl;

import com.example.POC_Github_APIs.model.*;
import com.example.POC_Github_APIs.service.IGithubService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GithubServiceImpl implements IGithubService {

    @Value("${GithubAppSettings.ClientID}")
    private String clientId;

    @Value("${GithubAppSettings.ClientSecret}")
    private String clientSecret;
    @Value("${GithubAppSettings.State}")
    private String state;
    @Value("${GithubAppSettings.Scope}")
    private String scope;
    @Value("${GithubAppSettings.RedirectURI}")
    private String redirectUri;
    @Value("${GithubApp.AuthorizeAPI}")
    private String GITHUB_AUTHORIZE_URL;
    @Value("${GithubApp.AccessTokenAPI}")
    private String GITHUB_ACCESS_TOKEN_URL;
    @Value("${GithubApp.BaseURL}")
    private String GITHUB_BASE_URL;
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();

    @Override
    public ResponseEntity getAccessToken(String code, String state) {
        if (!"randomStateString".equals(state)) {
            return ResponseEntity.badRequest().body(null);
        }
        // Exchange the code for an access token
//        Map<String, String> accessToken = exchangeCodeForAccessToken(code);
        ResponseEntity accessToken = exchangeCodeForAccessToken(code);
//        accessToken.get("")
//       String a= jwtTimeDurationConfig.generateToken(accessToken.get("access_token"));
//        System.out.println("a : "+a);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(accessToken);
    }

    @Override
    public ResponseEntity githubLoginRequest() {
        String authorizationURL = GITHUB_AUTHORIZE_URL + "?client_id=" + clientId + "&redirect_uri=" + redirectUri +
                "&scope=" + scope + "&state=" + state;
        headers.add(HttpHeaders.LOCATION, authorizationURL);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @Override
    public ResponseEntity orgUseListOfRepositories(RepositoryRequest repositoryRequest) {

        if (!state.equals(repositoryRequest.getGithubState())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid state");
        }
        if (!repositoryRequest.getOrgName().isBlank()) {
            String listOfRepoUrl = GITHUB_BASE_URL + "/orgs/" + repositoryRequest.getOrgName() + "/repos?per_page=200";
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + repositoryRequest.getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(listOfRepoUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().equals(HttpStatus.OK))
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Organization name cannot be blank");

    }

    @Override
    public List<BranchResponse> orgAndRepoUseListOfBranches(BranchRequest branchRequestDto) {

        int page = 1;
        int branchCount;
        List<BranchResponse> gitBranches = new ArrayList<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(branchRequestDto.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        do {
            String resourceUrl = GITHUB_BASE_URL + "/repos/" + branchRequestDto.getOrgName() + "/" + branchRequestDto.getRepoName() + "/branches?page=" + page + "&per_page=5";

            BranchResponse[] gitBranchesList = restTemplate.exchange(resourceUrl, HttpMethod.GET, entity, BranchResponse[].class).getBody();
            gitBranches.addAll(Arrays.asList(gitBranchesList)); // Avoid stream for readability and potential performance gains
            branchCount = gitBranchesList.length; // Direct length access for efficiency
            page++;
        } while (branchCount > 0);

        return gitBranches;
    }

    @Override
    public List<BranchFoldersResponse> branchGetAllFolders(BranchFolderRequest branchFolderRequestDto) {
        String resourceUrl = GITHUB_BASE_URL + "/repos/" + branchFolderRequestDto.getOrgName() + "/"
                + branchFolderRequestDto.getRepoName() + "/git/trees/" + branchFolderRequestDto.getBranchName() + "?recursive=1";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(branchFolderRequestDto.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<BranchFoldersResponse> gitBranchFolders = new ArrayList<>();
        var jsonTreeMap = restTemplate.exchange(resourceUrl, HttpMethod.GET, entity, Map.class).getBody();
        if (jsonTreeMap == null) {
            return gitBranchFolders;
        }

        var rootFolder = (List) jsonTreeMap.get("tree");
        List<Map<String, String>> folderList = rootFolder.stream().map(r -> (Map) r).toList();
        // Fetch the folders alone
        var folders = folderList.stream().filter(r -> r.get("type").equals("tree")).toList();

        int index = 0;
        for (
                Map<String, String> folder : folders) {
            BranchFoldersResponse foldersResponse = new BranchFoldersResponse();
            foldersResponse.setKey(index++);
            String basePath = folder.get("path");
            foldersResponse.setPath(basePath);
            var parentFolders = basePath.split("/");
            foldersResponse.setLabel(parentFolders[parentFolders.length - 1]);
            if (parentFolders.length == 1) {
                gitBranchFolders.add(foldersResponse);
            } else {
            }
            var fullParentFolders = Arrays.copyOf(parentFolders, parentFolders.length - 1);
            var selectedParent = findParentFolder(gitBranchFolders, String.join("/", fullParentFolders));
            if (selectedParent != null) {
                var children = selectedParent.getChildren();
                if (children == null) {
                    children = new ArrayList<>();
                    selectedParent.setChildren(children);
                }
                children.add(foldersResponse);
            }
        }
        return gitBranchFolders;
    }


    @Override
    public ResponseEntity cloneTheBranchProject(CloneBranchRequest cloneBranchRequest) {
        Map<String, String> response = new HashMap<>();
        File localPath = new File("/Users/thanikaivelp/Desktop/b/" + cloneBranchRequest.getRepoName());
        try {
            Git git = Git.cloneRepository()
                    .setURI(cloneBranchRequest.getRepoUrl())
                    .setDirectory(localPath)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(cloneBranchRequest.getAccessToken(), ""))
                    .setBranch(cloneBranchRequest.getCloneBranchName())
                    .call();
            response.put("message", "Repository cloned successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", "Failed to clone repository: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* @Override
     public ResponseEntity<Map<String, String>> NewBranchAndPushProject(PushNewBranchRequestModel pushNewBranchRequestModel) throws Exception {
         Map<String, String> response = new HashMap<>();

         try {
             // Open existing local repository
             File localPath = new File("/Users/thanikaivelp/Desktop/b/vulnerabilityRemediation/.git");
             Repository existingRepo = new FileRepository(localPath);
             Git git = new Git(existingRepo);


             git.branchCreate().setName(pushNewBranchRequestModel.getNewBranchName()).call();



             // Fetch latest branches from remote
             git.fetch()
                     .setRemote("main")
                     .setCredentialsProvider(new UsernamePasswordCredentialsProvider(pushNewBranchRequestModel.getAccessToken(), ""))
                     .call();

             // Create new branch from main/master
             git.checkout()
                     .setCreateBranch(true)
                     .setName(pushNewBranchRequestModel.getNewBranchName())
                     .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                     .setStartPoint("main") // Use the correct start point reference
                     .call();

             // Push the new branch to remote repository
             CredentialsProvider credentialsProvider =
                     new UsernamePasswordCredentialsProvider(pushNewBranchRequestModel.getAccessToken(), "");
             git.push()
                     .setCredentialsProvider(credentialsProvider)
                     .setRemote("origin")
                     .add(pushNewBranchRequestModel.getNewBranchName())
                     .call();

             response.put("message", "New branch '" + pushNewBranchRequestModel.getNewBranchName() + "' created and code pushed successfully");
             return ResponseEntity.ok(response);
         } catch (GitAPIException | IOException e) {
             response.put("error", "Failed to create branch and push code: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
         }
     }
 */
    @Override
    public ResponseEntity<Map<String, String>> NewBranchAndPushProject(PushNewBranchRequestModel pushNewBranchRequestModel) throws Exception {

        File repoDir = new File("/Users/thanikaivelp/Desktop/b/vulnerabilityRemediation/.git");
        String newBranchName = "new-branch-name"; // Replace with actual branch name

        try (Repository repository = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
            try (Git git = new Git(repository)) {
                try {
                    git.branchCreate().setName(newBranchName).call();
                    System.out.println("Branch created: " + newBranchName);
                } catch (RefAlreadyExistsException e) {
                    System.out.println("Branch already exists");
                } catch (InvalidRefNameException e) {
                    System.out.println("Invalid branch name");
                } catch (GitAPIException e) {
                    System.out.println("Git API Exception: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static BranchFoldersResponse findParentFolder(List<BranchFoldersResponse> gitBranchFolders, String basePath) {
        BranchFoldersResponse parentFolder = null;
        for (BranchFoldersResponse gitFolder : gitBranchFolders) {
            if (gitFolder.getPath().equals(basePath)) {
                return gitFolder;
            }
            if (gitFolder.getChildren() != null) {
                parentFolder = findParentFolder(gitFolder.getChildren(), basePath);
                if (parentFolder != null)
                    return parentFolder;
            }
        }
        return parentFolder;
    }


    private ResponseEntity exchangeCodeForAccessToken(String code) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("code", code);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(GITHUB_ACCESS_TOKEN_URL, requestEntity, Map.class);


        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            responseEntity.getBody();
            String date = responseEntity.getHeaders().get("Date").toString();
            System.out.println("Date : " + date);
            return responseEntity;
        } else {
            throw new RuntimeException("Failed to exchange code for access token: " + responseEntity.getStatusCode());
        }
    }

}
