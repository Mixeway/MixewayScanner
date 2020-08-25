package io.mixeway.scanner.integrations.scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.model.BanditResponse;
import io.mixeway.scanner.integrations.model.ProgPilotVuln;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.CodeHelper;
import io.mixeway.scanner.utils.Vulnerability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Progpilot PHP scanner integration - https://github.com/designsecurity/progpilot
 * @author gsiewruk
 */
@Component
public class Progpilot implements ScannerIntegrationFactory {
    private final static Logger log = LoggerFactory.getLogger(Progpilot.class);
    @Override
    public void prepare() throws Exception {

    }

    @Override
    public List<Vulnerability> runScan(ScanRequest scanRequest) throws Exception {
        log.info("[Progpilot] Starting to package app {}", scanRequest.getTarget());
        String projectDirectory = CodeHelper.getProjectPath(scanRequest, false);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "progpilot . > progpilot.vulns");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();
        log.info("[Progpilot] Scan completed");
        List<ProgPilotVuln> progPilotVulns = processProgPilotReport(projectDirectory + File.separatorChar + "progpilot.vulns");
        return convertProgpilotReportToVulns(progPilotVulns);
    }

    private List<Vulnerability> convertProgpilotReportToVulns(List<ProgPilotVuln> progPilotVulns) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        for (ProgPilotVuln progPilotVuln : progPilotVulns) {
            vulnerabilities.add(new Vulnerability(progPilotVuln));
        }
        return vulnerabilities;
    }

    @Override
    public List<Vulnerability> runScanStandalone() throws Exception {
        log.info("[Progpilot] Starting to package app ");
        String projectDirectory = CodeHelper.getProjectPath(new ScanRequest(), true);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "progpilot . > progpilot.vulns");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();
        log.info("[Progpilot] Scan completed");
        List<ProgPilotVuln> progPilotVulns = processProgPilotReport(projectDirectory + File.separatorChar + "progpilot.vulns");
        return convertProgpilotReportToVulns(progPilotVulns);
    }
    /**
     * Parsing bandit's json response into object
     *
     * @param reportPath path to bandit report
     */
    private List<ProgPilotVuln> processProgPilotReport(String reportPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(reportPath), new TypeReference<List<ProgPilotVuln>>(){});
    }
}
