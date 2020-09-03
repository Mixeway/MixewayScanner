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
    public void runScan() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        List<Vulnerability> vulnerabilityList = new ArrayList<>();
        SourceProjectType sourceProjectType = CodeHelper.getSourceProjectTypeFromDirectory(new ScanRequest(), true);
        if (sourceProjectType == null) {
            log.error("Repository doesnt contain any of the known types of projects. Current version support only JAVA-Maven projects.");
            System.exit(1);
        }
        try {
            checkMountPoint();
            // Running OpenSource Scan
            ScannerIntegrationFactory scannerIntegrationFactory = scannerFactory.getProperScanner(ScannerPluginType.DEPENDENCYTRACK);
            vulnerabilityList.addAll(scannerIntegrationFactory.runScanStandalone());
            log.info("Loaded DependencyTrack vulnerabilities - {}", vulnerabilityList.size());
            //Running SAST based on type of source
            switch (sourceProjectType) {
                case MAVEN:
                    ScannerIntegrationFactory spotbug = scannerFactory.getProperScanner(ScannerPluginType.SPOTBUG);
                    vulnerabilityList.addAll(spotbug.runScanStandalone());
                    break;
                case PIP:
                    ScannerIntegrationFactory bandit = scannerFactory.getProperScanner(ScannerPluginType.BANDIT);
                    vulnerabilityList.addAll(bandit.runScanStandalone());
                    break;
                case PHP:
                    ScannerIntegrationFactory progpilot = scannerFactory.getProperScanner(ScannerPluginType.PROGPILOT);
                    vulnerabilityList.addAll(progpilot.runScanStandalone());
                    break;
                default:
                    log.error("Source Code Language not supported");
                    System.exit(1);
            }
        } catch (Exception e ){
            log.error("[Standalone Mixeway App] Fatal error: {}", e.getLocalizedMessage());
        }
        if (StringUtils.isNotBlank(mixewayProjectName) && StringUtils.isNotBlank(commitId) && StringUtils.isNotBlank(branch)){
            mixewayConnector.sendRequestToMixewayStandalone(vulnerabilityList, mixewayProjectName, branch, commitId);
        } else {
            mixewayConnector.sendAnonymousRequestToMixeway(vulnerabilityList);
        }
        printResults(vulnerabilityList);
        writeResultsToFile(vulnerabilityList, CodeHelper.getProjectPath(new ScanRequest(), true));
    }

    private void writeResultsToFile(List<Vulnerability> vulnerabilityList, String directory) {
        try {
            Gson gson = new Gson();
            gson.toJson(vulnerabilityList, new FileWriter(directory + File.separator + "mixeway_sast_report.json"));
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
