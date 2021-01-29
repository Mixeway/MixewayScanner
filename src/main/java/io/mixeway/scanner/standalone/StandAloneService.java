package io.mixeway.scanner.standalone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import io.mixeway.scanner.factory.ScannerFactory;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class which run Standalone test. Gets current location as mounted application, check for language used and then
 * run full SAST scan
 *
 * @author gsiewruk
 */
@Service
public class StandAloneService {

    @Value("${BRANCH:}")
    String branch;
    @Value("${COMMIT_ID:}")
    String commitId;
    @Value("${MIXEWAY_PROJECT_NAME:}")
    String mixewayProjectName;
    private final static Logger log = LoggerFactory.getLogger(StandAloneService.class);
    ScannerFactory scannerFactory;
    MixewayConnector mixewayConnector;
    GitOperations gitOperations;
    public StandAloneService(ScannerFactory scannerFactory,
                             MixewayConnector mixewayConnector,
                             GitOperations gitOperations){
        this.scannerFactory = scannerFactory;
        this.mixewayConnector = mixewayConnector;
        this.gitOperations = gitOperations;
    }

    /**
     * Running scan in hardcoded location, first it check if location exists (is mounted during docker run),
     * and if yes it go full SAST scan, if not log info and exit.
     */
    public void runScan() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
        List<Vulnerability> vulnerabilityList = new ArrayList<>();
        String directory = CodeHelper.getProjectPath(new ScanRequest(), true);

        try {
            checkMountPoint();
            for (SourceProjectType projectType : SourceProjectType.values()){
                if (CodeHelper.isProjectInLanguage(directory, projectType)){
                    vulnerabilityList.addAll(scannerFactory.runScanForLanguage(projectType));
                }
            }
        } catch (Exception e ){
            e.printStackTrace();
            log.error("[Standalone Mixeway App] Fatal error: {}", e.getLocalizedMessage());
        }
        printResults(vulnerabilityList);
        writeResultsToFile(vulnerabilityList, CodeHelper.getProjectPath(new ScanRequest(), true));
        processMixeway(vulnerabilityList);
    }

    /**
     * Send vulnerabilities to Mixeway and waits for results if result is success exit with success, if failure exit with code 1
     *
     */
    private void processMixeway(List<Vulnerability> vulnerabilityList) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
        GitInformations gitInformations = GitOperations.getGitInformations();
        Status status;
        if (gitInformations != null){
            PrepareCIOperation prepareCiOperations = mixewayConnector.getCIInfo(new GetInfoRequest(gitInformations));
            status = mixewayConnector.sendRequestToMixewayWithGitInfo(gitInformations, prepareCiOperations, vulnerabilityList);
        } else if (StringUtils.isNotBlank(mixewayProjectName) && StringUtils.isNotBlank(commitId) && StringUtils.isNotBlank(branch)){
            status = mixewayConnector.sendRequestToMixewayStandalone(vulnerabilityList, mixewayProjectName, branch, commitId);
        } else {
            status = mixewayConnector.sendAnonymousRequestToMixeway(vulnerabilityList);
        }

        if (status != null && status.getStatus().equals(Constants.GATEWAY_SUCCESS)){
            System.exit(0);
        } else if (status == null) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    /**
     * Save results to file mixeway_sast_report.json
     * @param vulnerabilityList
     * @param directory
     */
    private void writeResultsToFile(List<Vulnerability> vulnerabilityList, String directory) {
        try {
            Gson gson = new Gson();
            Writer writer = null;
            try {
                writer = new FileWriter(directory + File.separator + "mixeway_sast_report.json");
                gson.toJson(vulnerabilityList, writer);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    } else {
                        log.error("Buffer has not been initialized!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error("Cannot write to {} check permission or use vulnerabilities from console", directory);
        }
    }

    /**
     * Printing results
     *
     * @param vulnerabilityList
     */
    private void printResults(List<Vulnerability> vulnerabilityList) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = mapper.writeValueAsString(vulnerabilityList);
        System.out.println(json);
    }

    /**
     * Checking if source directory is mounted properly:
     * -v ${PWD}:/opt/sources
     */
    public void checkMountPoint(){
        log.info("Running Standalone Mixeway Scanner App");
        if (Files.isDirectory(Paths.get(Constants.STANDALONE_DEFAULT_SOURCE_PATH))) {
            log.info("Directory is properly mounted proceeding...");
        } else {
            log.error("No location mounted exiting. Please make sure You have used '-v ${PWD}:/opt/sources'");
            System.exit(1);
        }
    }
}
