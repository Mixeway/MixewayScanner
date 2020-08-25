package io.mixeway.scanner.standalone;

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

    public StandAloneService(ScannerFactory scannerFactory){
        this.scannerFactory = scannerFactory;
    }

    /**
     * Running scan in hardcoded location, first it check if location exists (is mounted during docker run),
     * and if yes it go full SAST scan, if not log info and exit.
     */
    public void runScan() {
        SourceProjectType sourceProjectType = CodeHelper.getSourceProjectTypeFromDirectory(new ScanRequest(), true);
        if (sourceProjectType == null) {
            log.error("Repository doesnt contain any of the known types of projects. Current version support only JAVA-Maven projects.");
            System.exit(1);
        }
        try {
            checkMountPoint();
            // Running OpenSource Scan
            ScannerIntegrationFactory scannerIntegrationFactory = scannerFactory.getProperScanner(ScannerPluginType.DEPENDENCYTRACK);
            scannerIntegrationFactory.runScanStandalone();
            //Running SAST based on type of source
            switch (sourceProjectType) {
                case MAVEN:
                    ScannerIntegrationFactory spotbug = scannerFactory.getProperScanner(ScannerPluginType.SPOTBUG);
                    spotbug.runScanStandalone();
                    break;
                case PIP:
                    ScannerIntegrationFactory bandit = scannerFactory.getProperScanner(ScannerPluginType.BANDIT);
                    bandit.runScanStandalone();
                    break;
                default:
                    log.error("Source Code Language not supported");
                    System.exit(1);
            }
        } catch (Exception e ){
            log.error("[Standalone Mixeway App] Fatal error: {}", e.getLocalizedMessage());
        }
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
