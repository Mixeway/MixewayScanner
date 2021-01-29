package io.mixeway.scanner.utils;

/**
 * @author gsiewruk
 */
public class GetInfoRequest {
    String repoUrl;
    String branch;
    String scope;
    String repoName;

    public GetInfoRequest(GitInformations gitInformations) {
        this.repoUrl = gitInformations.getRepoUrl();
        this.branch = gitInformations.getBranchName();
        this.scope = "opensource";
        this.repoName=gitInformations.getProjectName();
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
