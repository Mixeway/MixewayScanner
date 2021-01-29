/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations.scanner;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.model.BugInstance;
import io.mixeway.scanner.integrations.model.SpotbugReportXML;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.rest.service.BaseService;
import io.mixeway.scanner.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
        log.info("[Spotbug] Starting to generate Spotbug report for {}", scanRequest.getTarget());
        ProcessBuilder spotbug = new ProcessBuilder("bash",
                "-c",
                "mvn compile -DskipTests com.github.spotbugs:spotbugs-maven-plugin:spotbugs").inheritIO();
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
            if (reportXML.getBugInstanceList() !=null) {
                for (BugInstance bugInstance : reportXML.getBugInstanceList()) {
                    vulnerabilities.add(new Vulnerability(bugInstance,
                            reportXML
                                    .getBugPatterns()
                                    .stream()
                                    .filter(bugPattern -> bugPattern.getShortDescriptions().equals(bugInstance.getShortDescription()))
                                    .findFirst()
                                    .orElse(null)));
                }
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
            log.info("[Spotbug] Preparing POM, create backup and generate new");
            preparePomforSpotbugAnalysis(projectDirectory);
            log.info("[Spotbug] Starting to generate Spotbug report for {}", projectDirectory);
            ProcessBuilder spotbug = new ProcessBuilder("bash",
                    "-c",
                    "mvn compile -DskipTests spotbugs:spotbugs").inheritIO();
            spotbug.directory(new File(projectDirectory));
            Process spotbugProcess = spotbug.start();
            spotbugProcess.waitFor(5, TimeUnit.MINUTES);
            spotbugProcess.destroy();
            spotbugProcess.waitFor();
            log.info("[Spotbug] Report ready to process {}", projectDirectory);
            searchForReports(spotbugReportXMLS, projectDirectory);
            clearAfterPomManipulation(projectDirectory);
            log.info("[Spotbug] Scan completed, artifact cleared");
            return convertSpotbugReportIntoVulnList(spotbugReportXMLS);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[Spotbug] Error occuredd during scanning reason {} on line {}", e.getLocalizedMessage(), e.getStackTrace()[0].getLineNumber());
            return new ArrayList<>();
        }
    }
    public List<Vulnerability> runScanStandalone(String directory) throws IOException, InterruptedException {
        try {
            List<SpotbugReportXML> spotbugReportXMLS = new ArrayList<>();
            List<Path> reportPaths = new ArrayList<>();
            String projectDirectory = directory!=null ? directory : CodeHelper.getProjectPath(new ScanRequest(), true);
            log.info("[Spotbug] Preparing POM, create backup and generate new");
            preparePomforSpotbugAnalysis(projectDirectory);
            log.info("[Spotbug] Starting to generate Spotbug report for {}", projectDirectory);
            ProcessBuilder spotbug = new ProcessBuilder("bash",
                    "-c",
                    "mvn compile -DskipTests spotbugs:spotbugs").inheritIO();
            spotbug.directory(new File(projectDirectory));
            Process spotbugProcess = spotbug.start();
            spotbugProcess.waitFor(5, TimeUnit.MINUTES);
            spotbugProcess.destroy();
            spotbugProcess.waitFor();
            log.info("[Spotbug] Report ready to process {}", projectDirectory);
            searchForReports(spotbugReportXMLS, projectDirectory);
            clearAfterPomManipulation(projectDirectory);
            log.info("[Spotbug] Scan completed, artifact cleared");
            return convertSpotbugReportIntoVulnList(spotbugReportXMLS);
        } catch (Exception e) {
            e.printStackTrace();
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

    /**
     * First builds spotbugs build with plugins completed
     * Second replace build tag in pom with new one
     * third create backup of old pom.xml
     * end create new pom
     * @param directory to look for pom
     */
    private void preparePomforSpotbugAnalysis(String directory) throws IOException, ParserConfigurationException, SAXException, TransformerException, InterruptedException {
        File file = new File(directory + File.separatorChar + "pom.xml");
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        List<PomPlugin> plugins = new ArrayList<>();
        plugins.add(buildSpotbugPlugin());

        String replaceWith = "<build>" + xmlMapper.
                writer()
                .withRootName("plugins")
                .writeValueAsString(plugins).replaceAll("item","plugin") + "</build>";

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        NodeList buildNode = doc.getElementsByTagName("project");
        Element element = dbFactory.newDocumentBuilder().parse(new InputSource(new StringReader(
                replaceWith))).getDocumentElement();
        Node firstDocImportedNode = doc.importNode(element, true);

        boolean spotbugsAdded = false;
        for(int i=0; i<buildNode.getLength(); i++) {
            NodeList projectNodes = buildNode.item(i).getChildNodes();
            for (int j=0; j< projectNodes.getLength(); j++) {
                if (projectNodes.item(j).getNodeName().equals("build")){
                    projectNodes.item(j).getParentNode().replaceChild(firstDocImportedNode, projectNodes.item(j));
                    spotbugsAdded = true;
                }
            }
        }
        if (!spotbugsAdded){
            doc.getElementsByTagName("project").item(0).appendChild(firstDocImportedNode);
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(doc), new StreamResult(sw));

        backupPom(directory);
        createNewPom(sw.toString(), directory);
    }

    /**
     * Create new Pom with spotbugs plugin enabled
     */
    private void createNewPom(String pom, String directory) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(directory + File.separatorChar + "pom.xml"));
        writer.write(pom);

        writer.close();
    }

    /**
     * remove tmp pom.xml and replace old one
     */
    private void clearAfterPomManipulation(String s) throws IOException, InterruptedException {
        ProcessBuilder spotbug = new ProcessBuilder("bash",
                "-c",
                "mv pom.xml.bak pom.xml && rm spotbugs-security-include.xml && rm spotbugs-security-exclude.xml");
        spotbug.directory(new File(s));
        Process spotbugProcess = spotbug.start();
        spotbugProcess.waitFor();
    }

    /**
     * Create copy of pom.xml
     * and create Spobugs security XMLs
     */
    private void backupPom(String s) throws IOException, InterruptedException {
        ProcessBuilder spotbug = new ProcessBuilder("bash",
                "-c",
                "mv pom.xml pom.xml.bak");
        spotbug.directory(new File(s));
        Process spotbugProcess = spotbug.start();
        spotbugProcess.waitFor();
        //Create spotbugs-security-include.xml
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(s + File.separatorChar + "spotbugs-security-include.xml"), StandardCharsets.UTF_8))) {
            writer.write("<FindBugsFilter><Match><Bug category=\"SECURITY\"/></Match></FindBugsFilter>");
        }
        //Create spotbugs-security-exclude.xml
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(s + File.separatorChar + "spotbugs-security-exclude.xml"), StandardCharsets.UTF_8))) {
            writer.write("<FindBugsFilter></FindBugsFilter>");
        }
    }


    /**
     * Create Spotbug element which will be appended into Pom.xml
     * @return configured spotbugs with find-sec-bugs
     */
    public PomPlugin buildSpotbugPlugin(){
        List<PomPlugin> findSecBugList = new ArrayList<>();
        PomPlugin findSecBugs = PomPlugin.builder()
                .groupId("com.h3xstream.findsecbugs")
                .artifactId("findsecbugs-plugin")
                .version("1.10.1")
                .build();
        findSecBugList.add(findSecBugs);
        PomConfiguration configuration = PomConfiguration.builder()
                .effort("Max")
                .threshold("Low")
                .failOnError("true")
                .plugins(findSecBugList)
                .build();
        return PomPlugin.builder()
                .groupId("com.github.spotbugs")
                .artifactId("spotbugs-maven-plugin")
                .version("4.0.4")
                .configuration(configuration)
                .build();
    }
}
