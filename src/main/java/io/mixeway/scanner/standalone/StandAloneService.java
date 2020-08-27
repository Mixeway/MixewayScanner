package io.mixeway.scanner.standalone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.mixeway.scanner.factory.ScannerFactory;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
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
    private final static Logger log = LoggerFactory.getLogger(StandAloneService.class);
    ScannerFactory scannerFactory;
    MixewayConnector mixewayConnector;
    public StandAloneService(ScannerFactory scannerFactory,
                             MixewayConnector mixewayConnector){
        this.scannerFactory = scannerFactory;
        this.mixewayConnector = mixewayConnector;
    }

    /**
     * Running scan in hardcoded location, first it check if location exists (is mounted during docker run),
     * and if yes it go full SAST scan, if not log info and exit.
     */
    public void runScan() throws JsonProcessingException {
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
        mixewayConnector.sendAnonymousRequestToMixeway(vulnerabilityList);
        printResults(vulnerabilityList);
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
