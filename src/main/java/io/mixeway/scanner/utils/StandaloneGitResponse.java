package io.mixeway.scanner.utils;

import lombok.Builder;
import lombok.Getter;

/**
 * @author gsiewruk
 */
@Builder
@Getter
public class StandaloneGitResponse {
    private String branch;
    private String commitId;
    private String projectName;

    public StandaloneGitResponse(String branch, String commitId, String projectName) {
        this.branch = branch;
        this.commitId = commitId;
        this.projectName = projectName;
    }

    public StandaloneGitResponse() {

    }

    @Override
    public String toString() {
        return "StandaloneGitResponse{" +
                "branch='" + branch + '\'' +
                ", commitId='" + commitId + '\'' +
                ", projectName='" + projectName + '\'' +
                '}';
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
