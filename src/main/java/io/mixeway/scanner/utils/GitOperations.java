/*
 * @created  2020-08-19 : 18:30
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.utils;

import io.mixeway.scanner.rest.model.ScanRequest;
import org.eclipse.jgit.api.ListBranchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class which enable integration with GIT Repositories
 */
@Component
public class GitOperations {
    @Value( "${sources.location}" )
    private String sourceLocation;
    private final Logger log = LoggerFactory.getLogger(GitOperations.class);

    /**
     * Method which is pulling git repo based on scanRequest
     *
     * @param scanRequest with repo url and auth credentials
     * @return object with commit id for given branch
     */
    public GitResponse pull(ScanRequest scanRequest) throws Exception {
        try {
            verifyLocation();
        } catch (Exception e){
            throw new Exception (e.getLocalizedMessage());
        }
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            CredentialsProvider credentialsProvider = prepareCredentails(scanRequest);

            Repository repository = builder.setGitDir(Paths.get(sourceLocation + File.separatorChar + CodeHelper.getNameFromRepoUrlforSAST(scanRequest.getTarget(), false) + File.separatorChar + ".git").toFile())
                    .readEnvironment()
                    .findGitDir()
                    .build();
            Git git = new Git(repository);
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            git.pull()
                    .setRemote("origin")
                    .setStrategy(MergeStrategy.THEIRS)
                    .setCredentialsProvider(credentialsProvider)
                    .setRemoteBranchName(scanRequest.getBranch() != null ? scanRequest.getBranch() : Constants.GIT_BRANCH_MASTER)
                    .call();
            Ref call = git.checkout().setName("origin/" + (scanRequest.getBranch() != null ? scanRequest.getBranch() : Constants.GIT_BRANCH_MASTER)).call();
            String commitId = git.log().setMaxCount(1).call().iterator().next().getName();
            log.info("[GIT] Successfully fetched repo for {} commitId id is {} branch {}", scanRequest.getTarget(), commitId, scanRequest.getBranch());
            return new GitResponse(true,commitId);
        } catch (GitAPIException | IOException e){
            log.error("[GIT] Error during fetching repo {}", e.getLocalizedMessage());
        }
        return new GitResponse(false,"");
    }

    /**
     * Method which clone repository based on scanRequest
     * @param scanRequest given scanRequest with repo url and auth credentials
     * @return commitid of given branch
     */
    public GitResponse clone(ScanRequest scanRequest) throws Exception {
        try {
            verifyLocation();
        } catch (Exception e){
            throw new Exception (e.getLocalizedMessage());
        }
        try {
            Git git = Git.cloneRepository()
                    .setCredentialsProvider(prepareCredentails(scanRequest))
                    .setURI(scanRequest.getTarget() + ".git")
                    .setDirectory(Paths.get(sourceLocation + File.separatorChar + CodeHelper.getNameFromRepoUrlforSAST(scanRequest.getTarget(), false)).toFile())
                    .call();
            Ref call = git.checkout().setName("origin/" + (scanRequest.getBranch() != null ? scanRequest.getBranch() : Constants.GIT_BRANCH_MASTER)).call();
            String commitId = git.log().setMaxCount(1).call().iterator().next().getName();
            log.info("[GIT] Successfully cloned repo for {} commitId is {} branch {}", scanRequest.getTarget(), commitId, scanRequest.getBranch());
            return new GitResponse(true, commitId);
        } catch (GitAPIException e){
            log.error("[GIT] Error cloning repo {}", e.getLocalizedMessage());
        }
        return new GitResponse(false, "");
    }

    /**
     * Preparing CredentialsProvider for given auth info:
     *  - No auth if no pass and user given
     *  - Token auth if only pass provided
     *  - basic auth if both pass and user provided
     * @param scanRequest scan request to prepare credentials for
     * @return credentials provided object
     */
    private CredentialsProvider prepareCredentails(ScanRequest scanRequest) {
        if (scanRequest.getUsername() ==null && scanRequest.getPassword()!=null) {
            return new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", scanRequest.getPassword());
        } else if (scanRequest.getUsername() !=null && scanRequest.getPassword()!=null ) {
            return new UsernamePasswordCredentialsProvider(scanRequest.getUsername(), scanRequest.getPassword());
        } else {
            return null;
        }
    }

    /**
     * Method which verify if location and configuration for given request is already configured
     *
     * @param scanRequest scan request with target to verify
     * @return boolean if location exists or not
     */
    public boolean isProjectPresent(ScanRequest scanRequest){
        Path path = Paths.get(sourceLocation + File.separatorChar + CodeHelper.getNameFromRepoUrlforSAST(scanRequest.getTarget(), false));
        return Files.exists(path);
    }

    /**
     * Method which verify if sourcesLocation exist on FileSystem. Directory is created if needed.
     * Permissions to write on given location is required.
     */
    private void verifyLocation() throws Exception {
        Path path = Paths.get(sourceLocation);
        if (!Files.exists(path)) {
            File file = new File(sourceLocation);
            if (!file.exists()) {
                if (file.mkdir()) {
                    log.info("[GIT] Directory is created! {}", sourceLocation);
                } else {
                    log.warn("[GIT] Failed to create directory {}", sourceLocation);
                    throw new Exception("Cannot create directory");
                }
            }
        }
    }

}
