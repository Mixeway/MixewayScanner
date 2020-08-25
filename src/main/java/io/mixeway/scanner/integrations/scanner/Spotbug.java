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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

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
    public void runScan(ScanRequest scanRequest) throws IOException, InterruptedException {
        log.info("[Spotbug] Starting to package app {}", scanRequest.getTarget());
        String projectDirectory = CodeHelper.getProjectPath(scanRequest, false);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "mvn package -DskipTests");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();
        log.info("[Spotbug] Starting to generate Spotbug report for {}", scanRequest.getTarget());
        ProcessBuilder spotbug = new ProcessBuilder("bash", "-c", "mvn com.github.spotbugs:spotbugs-maven-plugin:spotbugs");
        spotbug.directory(new File(projectDirectory));
        Process spotbugProcess = spotbug.start();
        spotbugProcess.waitFor();
        log.info("[Spotbug] Report ready to process {}", scanRequest.getTarget());
        SpotbugReportXML spotbugReportXML = processXmlReport(projectDirectory + File.separatorChar + "target" + File.separatorChar + "spotbugsXml.xml");
        log.info("[Spotbug] Detected vulnerabilities: ");
        for (BugInstance bugInstance : spotbugReportXML.getBugInstanceList()){
            log.info("Location {}, severity {}, cwe {}, name {}, description {}", bugInstance.getSourceLine().getSourcepath(), bugInstance.getPriority(), bugInstance.getCweid(),
                    bugInstance.getCategory(), bugInstance.getLongMessage());
        }

    }

    @Override
    public void runScanStandalone() throws IOException, InterruptedException {
        String projectDirectory = CodeHelper.getProjectPath(new ScanRequest(), true);
        log.info("[Spotbug] Starting to package app {}", projectDirectory);
        ProcessBuilder packageApp = new ProcessBuilder("bash", "-c", "mvn package -DskipTests");
        packageApp.directory(new File(projectDirectory));
        Process packageAppProcess = packageApp.start();
        packageAppProcess.waitFor();
        log.info("[Spotbug] Starting to generate Spotbug report for {}", projectDirectory);
        ProcessBuilder spotbug = new ProcessBuilder("bash", "-c", "mvn com.github.spotbugs:spotbugs-maven-plugin:spotbugs");
        spotbug.directory(new File(projectDirectory));
        Process spotbugProcess = spotbug.start();
        spotbugProcess.waitFor();
        log.info("[Spotbug] Report ready to process {}", projectDirectory);
        SpotbugReportXML spotbugReportXML = processXmlReport(projectDirectory + File.separatorChar + "target" + File.separatorChar + "spotbugsXml.xml");
        log.info("[Spotbug] Detected vulnerabilities: ");
        for (BugInstance bugInstance : spotbugReportXML.getBugInstanceList()){
            log.info("Location {}, severity {}, cwe {}, name {}, description {}", bugInstance.getSourceLine().getSourcepath(), bugInstance.getPriority(), bugInstance.getCweid(),
                    bugInstance.getCategory(), bugInstance.getLongMessage());
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
