package io.mixeway.scanner.integrations.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.model.BanditResponse;
import io.mixeway.scanner.integrations.model.BanditResult;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.CodeHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Bandit Python scanner integration - https://github.com/PyCQA/bandit
 * @author gsiewruk
 */
@Component
public class Bandit implements ScannerIntegrationFactory {
    private final static Logger log = LoggerFactory.getLogger(Spotbug.class);
    @Override
    public void prepare() throws Exception {

    }

    /**
     * Running Bandit scan with 'bandit -r . --format json' and then parse json response into object
     *
     * @param scanRequest to get directory
     * @throws Exception
     */
    @Override
    public void runScan(ScanRequest scanRequest) throws Exception {
        log.info("[Bandit] Starting to Scan app {}", scanRequest.getTarget());
        String projectDirectory = CodeHelper.getProjectPath(scanRequest, false);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "bandit -r . --format json > bandit.vulns");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();

        BanditResponse banditResponse = processBanditReport(projectDirectory + File.separatorChar + "bandit.vulns");
        for (BanditResult banditResult :banditResponse.getResults()){
            log.warn("Name: {} file {}:{}, severity {}", banditResult.getTest_name(),banditResult.getFilename(),banditResult.getLine_number(), banditResult.getIssue_severity());
        }
    }

    /**
     * Parsing bandit's json response into object
     *
     * @param reportPath path to bandit report
     */
    private BanditResponse processBanditReport(String reportPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(reportPath), BanditResponse.class);
    }

    @Override
    public void runScanStandalone() throws Exception {
        log.info("[Bandit] Starting to Scan app ");
        String projectDirectory = CodeHelper.getProjectPath(new ScanRequest(), true);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "bandit -r . --format json > bandit.vulns");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();

        BanditResponse banditResponse = processBanditReport(projectDirectory + File.separatorChar + "bandit.vulns");
        for (BanditResult banditResult :banditResponse.getResults()){
            log.warn("Name: {} file {}:{}, severity {}", banditResult.getTest_name(),banditResult.getFilename(),banditResult.getLine_number(), banditResult.getIssue_severity());
        }
    }
}
