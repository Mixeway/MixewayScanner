/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations.scanner;


import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.model.BugInstance;
import io.mixeway.scanner.integrations.model.SpotbugReportXML;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.rest.service.BaseService;
import io.mixeway.scanner.utils.CodeHelper;
import io.mixeway.scanner.utils.SourceProjectType;
import io.mixeway.scanner.utils.Vulnerability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Spotbug implements ScannerIntegrationFactory {
    private final static Logger log = LoggerFactory.getLogger(Spotbug.class);
    @Override
    public void prepare() {

    }

    /**
     * Running proper command
     * @param scanRequest
     */
    @Override
    public List<Vulnerability> runScan(ScanRequest scanRequest) throws IOException, InterruptedException {
        log.info("[Spotbug] Starting to package app {}", scanRequest.getTarget());
        List<SpotbugReportXML> spotbugReportXMLS = new ArrayList<>();
        List<Path> reportPaths = new ArrayList<>();
        String projectDirectory = CodeHelper.getProjectPath(scanRequest, false);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "mvn package -DskipTests");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();
        log.info("[Spotbug] Starting to generate Spotbug report for {}", scanRequest.getTarget());
        ProcessBuilder spotbug = new ProcessBuilder("bash", "-c", "mvn com.github.spotbugs:spotbugs-maven-plugin:spotbugs");
        spotbug.directory(new File(projectDirectory));
        Process spotbugProcess = spotbug.inheritIO().start();
        spotbugProcess.waitFor();
        log.info("[Spotbug] Report ready to process {}", scanRequest.getTarget());
        SpotbugReportXML spotbugReportXML = processXmlReport(projectDirectory + File.separatorChar + "target" + File.separatorChar + "spotbugsXml.xml");
        log.info("[Spotbug] Scan completed");
        searchForReports(spotbugReportXMLS, projectDirectory);
        return convertSpotbugReportIntoVulnList(spotbugReportXMLS);
    }

    private void searchForReports(List<SpotbugReportXML> spotbugReportXMLS, String projectDirectory) throws IOException {
        List<Path> reportPaths;
        try (Stream<Path> paths = Files.walk(Paths.get(projectDirectory))) {
            reportPaths = paths
                    .filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().equals("spotbugsXml.xml"))
                    .collect(Collectors.toList());
        }
        for (Path path : reportPaths){
            spotbugReportXMLS.add(processXmlReport(path.toString()));
        }
    }

    /**
     * Converts spotbug XML report into Vulnerabilities
     * @param spotbugReportXMLs report to parse
     * @return list of shared items
     */
    private List<Vulnerability> convertSpotbugReportIntoVulnList(List<SpotbugReportXML> spotbugReportXMLs) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        for (SpotbugReportXML reportXML : spotbugReportXMLs) {
            for (BugInstance bugInstance : reportXML.getBugInstanceList()) {
                vulnerabilities.add(new Vulnerability(bugInstance));
            }
        }
        return vulnerabilities;
    }

    @Override
    public List<Vulnerability> runScanStandalone() throws IOException, InterruptedException {
        try {
            List<SpotbugReportXML> spotbugReportXMLS = new ArrayList<>();
            List<Path> reportPaths = new ArrayList<>();
            String projectDirectory = CodeHelper.getProjectPath(new ScanRequest(), true);
            log.info("[Spotbug] Starting to generate Spotbug report for {}", projectDirectory);
            ProcessBuilder spotbug = new ProcessBuilder("bash", "-c", "mvn compile -DskipTests com.github.spotbugs:spotbugs-maven-plugin:spotbugs");
            spotbug.directory(new File(projectDirectory));
            Process spotbugProcess = spotbug.start();
            spotbugProcess.waitFor(5, TimeUnit.MINUTES);
            spotbugProcess.destroy();
            spotbugProcess.waitFor();
            log.info("[Spotbug] Report ready to process {}", projectDirectory);
            searchForReports(spotbugReportXMLS, projectDirectory);

            log.info("[Spotbug] Scan completed");
            return convertSpotbugReportIntoVulnList(spotbugReportXMLS);
        } catch (Exception e) {
            log.error("[Spotbug] Error occuredd during scanning reason {} on line {}", e.getLocalizedMessage(), e.getStackTrace()[0].getLineNumber());
            return new ArrayList<>();
        }
    }

    /**
     * Method which takes directory for spotbugXml.xml report file and convert it into readable object
     *
     * @param directory to spotbugXml.xml file
     * @return object with defects
     */
    private SpotbugReportXML processXmlReport(String directory){
        File xmlFile = new File(directory);
        JAXBContext jaxbContext;
        try
        {
            jaxbContext = JAXBContext.newInstance(SpotbugReportXML.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return ((SpotbugReportXML) jaxbUnmarshaller.unmarshal(xmlFile)).processSeverity();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
