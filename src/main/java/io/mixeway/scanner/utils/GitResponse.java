package io.mixeway.scanner.utils;

public class GitResponse {
    private Boolean status;
    private String commitId;

    public GitResponse(Boolean status, String commitId){
        this.commitId = commitId;
        this.status = status;
    }
    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
}
