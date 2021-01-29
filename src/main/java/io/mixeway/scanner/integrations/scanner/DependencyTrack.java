/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations.scanner;

import io.mixeway.scanner.db.entity.DependencyTrackEntity;
import io.mixeway.scanner.db.repository.DependencyTrackRepository;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.model.*;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.Constants;
import io.mixeway.scanner.utils.CodeHelper;
import io.mixeway.scanner.utils.SourceProjectType;
import io.mixeway.scanner.utils.Vulnerability;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hobsoft.spring.resttemplatelogger.LoggingCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class DependencyTrack implements ScannerIntegrationFactory {
    private final static Logger log = LoggerFactory.getLogger(DependencyTrack.class);
    private DependencyTrackRepository dependencyTrackRepository;
    @Value("${sonatype.oss.username}")
    private String ossUsername;
    @Value("${sonatype.oss.key}")
    private String ossKey;

    @Autowired
    public DependencyTrack(DependencyTrackRepository dependencyTrackRepository){
        this.dependencyTrackRepository = dependencyTrackRepository;
    }

    public DependencyTrack(){}

    /**
     * Verify if Dependency Track is initialized - DependencyTrack has enabled=true and apiKey not null
     */
    @Override
    public void prepare() throws Exception {
        Optional<DependencyTrackEntity> dependencyTrackEntity = dependencyTrackRepository.findByEnabledAndApiKeyNotNull(true);
        if (!dependencyTrackEntity.isPresent()){
            changePassword();
            getApiKey();
            setOssIntegration();
        }
    }

    /**
     * Loading vulnerabilities from dependency-track
     * @param dependencyTrack to get url and api
     * @param uuid of project to check
     */
    private List<DTrackVuln> loadVulnerabilities(DependencyTrackEntity dependencyTrack, String uuid ){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.DEPENDENCYTRACK_APIKEY_HEADER, dependencyTrack.getApiKey());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<DTrackVuln>> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                    Constants.DEPENDENCYTRACK_URL_VULNS + uuid, HttpMethod.GET, entity, new ParameterizedTypeReference<List<DTrackVuln>>() {
            });
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            log.error("[Dependency Track] Unable to get Findings from Dependency Track for project {}", uuid);
        }
        return null;
    }

    /**
     * Setting sonatype integration
     *
     */
    private void setOssIntegration() throws Exception {
        Optional<DependencyTrackEntity> dependencyTrack = dependencyTrackRepository.findByEnabledAndApiKeyNotNull(false);
        if (!dependencyTrack.isPresent()) {
            throw new Exception("[Dependency Track] Cannot change config for not initialized scanner");
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + getOAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DTrackConfigProperty[]> entity = new HttpEntity<>(prepareOssIntegration(ossUsername,ossKey),headers);
        ResponseEntity<String> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                Constants.DEPENDENCYTRACK_URL_OSS_CONFIG, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            dependencyTrack.get().setEnabled(true);
            dependencyTrackRepository.save(dependencyTrack.get());
            log.info("[Dependency Track] Successfully set OSS integration. DependencyTrack activated");
        }
    }

    /**
     * Preparing request body for seting sonatype oss integration
     *
     * @param ossUsername username taken from ENV variable
     * @param ossKey apiKey taken from ENV variable
     * @return array of elements
     */
    private DTrackConfigProperty[] prepareOssIntegration(String ossUsername, String ossKey) {
        return new DTrackConfigProperty[]{
                new DTrackConfigProperty("scanner","ossindex.enabled", true),
                new DTrackConfigProperty("scanner","ossindex.api.username", ossUsername),
                new DTrackConfigProperty("scanner","ossindex.api.token", ossKey)
        };
    }

    /**
     * Using obtained oAuth token to check apiKey which is set for Automation team and then save it to DB
     */
    private void getApiKey() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + getOAuthToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<DependencyTrackConfiguration[]> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                Constants.DEPENDENCYTRACK_URL_APIKEY, HttpMethod.GET, entity, DependencyTrackConfiguration[].class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            List<DependencyTrackConfiguration> dependencyTrackConfigurations = Arrays.asList(response.getBody());
            String apiKey = dependencyTrackConfigurations
                    .stream()
                    .filter(c -> c.getName().equals(Constants.DEPENDENCYTRACK_AUTOMATION))
                    .findFirst()
                    .orElse(null)
                    .getApiKeys()
                    .stream().findFirst().orElse(null)
                    .getKey();
            String automationTeamUuid = dependencyTrackConfigurations
                    .stream()
                    .filter(c -> c.getName().equals(Constants.DEPENDENCYTRACK_AUTOMATION))
                    .findFirst()
                    .orElse(null)
                    .getUuid();
            setPermissions(automationTeamUuid);
            dependencyTrackRepository.save(new DependencyTrackEntity(apiKey));
            log.info("[Dependency Track] Successfully saved apiKey");
        }

    }

    /**
     * Default API user doesn't have permission to view or create projects, those hase to be added
     *
     * @param automationTeamUuid
     */
    private void setPermissions(String automationTeamUuid) {
        RestTemplate restTemplate =  new RestTemplateBuilder()
                .customizers(new LoggingCustomizer())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getOAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}",headers);
        String[] permissions = new String[] {"ACCESS_MANAGEMENT", "BOM_UPLOAD","PORTFOLIO_MANAGEMENT","PROJECT_CREATION_UPLOAD","SYSTEM_CONFIGURATION","VIEW_PORTFOLIO","VULNERABILITY_ANALYSIS"};
        for (String permision : permissions) {
            ResponseEntity<String> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                    Constants.DEPENDENCYTRACK_URL_PERMISSIONS + permision + "/team/" + automationTeamUuid, HttpMethod.POST, entity, String.class);

        }
        log.info("[Dependency Track] Permission for API enabled");
    }


    /**
     * whole scan logic, get UUID or create project, generate BOM, upload it to DTrack and then load vulnerabilities
     *
     * @throws Exception
     */
    @Override
    public List<Vulnerability> runScan(ScanRequest scanRequest) throws Exception {
        this.prepare();
        Optional<DependencyTrackEntity> dependencyTrack = dependencyTrackRepository.findByEnabledAndApiKeyNotNull(true);
        if(dependencyTrack.isPresent()) {
            String uuid = getDTrackProjectUuid(dependencyTrack.get(),scanRequest, false);
            SourceProjectType sourceProjectType = CodeHelper.getSourceProjectTypeFromDirectory(scanRequest, false);
            if (sourceProjectType == null){
                throw new Exception("Unknown project type. Supported: MVN, NPM, Composer, PIP");
            }
            log.info("[Dependency Track] Get UUID {} and type of project {}", uuid, sourceProjectType);
            String bomPath = generateBom(scanRequest, sourceProjectType, false, null);
            if (bomPath == null) {
                throw new Exception("SBOM path appears to be null");
            }
            sendBomToDTrack(dependencyTrack.get(), uuid, bomPath);
            //Sleep untill DTrack audit the bom
            TimeUnit.SECONDS.sleep(50);
            log.info("[Dependency Track] Scan completed");
            return convertDTrackResponseToVulnerabilities(loadVulnerabilities(dependencyTrack.get(),uuid));
        } else {
            log.error("[Dependency Track] Trying to run scan on not properly initialized scanner. " +
                    "This should not happen, please collect log and issue ticket.");
        }
        return new ArrayList<>();
    }


    private List<Vulnerability> convertDTrackResponseToVulnerabilities(List<DTrackVuln> loadVulnerabilities) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        for (DTrackVuln dTrackVuln : loadVulnerabilities){
            vulnerabilities.add(new Vulnerability(dTrackVuln));
        }
        return  vulnerabilities;
    }

    /**
     * Running standalone scan
     */
    @Override
    public List<Vulnerability> runScanStandalone() throws Exception {
        this.prepare();
        Optional<DependencyTrackEntity> dependencyTrack = dependencyTrackRepository.findByEnabledAndApiKeyNotNull(true);
        if(dependencyTrack.isPresent()) {
            String uuid = getDTrackProjectUuid(dependencyTrack.get(),new ScanRequest(), true);
            SourceProjectType sourceProjectType = CodeHelper.getSourceProjectTypeFromDirectory(new ScanRequest(), true);
            if (sourceProjectType == null){
                throw new Exception("Unknown project type. Supported: MVN, NPM, Composer, PIP");
            }
            log.info("[Dependency Track] Get UUID {} and type of project {}", uuid, sourceProjectType);
            String bomPath = generateBom(new ScanRequest(), sourceProjectType, true,null);
            if (bomPath == null) {
                throw new Exception("SBOM path appears to be null");
            }
            sendBomToDTrack(dependencyTrack.get(), uuid, bomPath);
            //Sleep untill DTrack audit the bom
            TimeUnit.SECONDS.sleep(60);
            log.info("[Dependency Track] Scan completed");
            return convertDTrackResponseToVulnerabilities(loadVulnerabilities(dependencyTrack.get(),uuid));
        } else {
            log.error("[Dependency Track] Trying to run scan on not properly initialized scanner. " +
                    "This should not happen, please collect log and issue ticket.");
        }
        return new ArrayList<>();
    }

    /**
     * Method which uploads SBOM to Dependency Track
     *
     * @param dependencyTrackEntity for URL and api key
     * @param uuid UUID of project on DTrack to attach SBOM
     * @param bomPath location of file to upload
     */
    private void sendBomToDTrack(DependencyTrackEntity dependencyTrackEntity, String uuid, String bomPath) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(Constants.DEPENDENCYTRACK_APIKEY_HEADER, dependencyTrackEntity.getApiKey());
        HttpEntity<SendBomRequest> entity = new HttpEntity<>(new SendBomRequest(uuid, encodeFileToBase64Binary(bomPath)), headers);
        ResponseEntity<String> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                Constants.DEPENDENCYTRACK_URL_UPLOAD_BOM, HttpMethod.PUT, entity, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            log.info("[Dependency Track] SBOM for {} uploaded successfully", uuid);
        }
    }

    /**
     * Encodes file content to base64
     * @param fileName file name to encode
     * @return return base64 string
     * @throws IOException
     */
    private static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    /**
     * Generation of SBOM using CycloneDX plugin which depend on technology. Required for this method is language of project and path to location
     *
     * @param scanRequest needed to get location of source code
     * @param sourceProjectType needed to determine which type of execution to be done
     * @return return path to SBOM file
     */
    private String generateBom(ScanRequest scanRequest, SourceProjectType sourceProjectType, boolean standalone, String path) throws IOException, InterruptedException {
        String directory = path!=null ? path : CodeHelper.getProjectPath(scanRequest, standalone);
        ProcessBuilder install, generate;
        Process installProcess, generateProcess;

        switch (sourceProjectType) {
            case PIP:
                ProcessBuilder freeze = new ProcessBuilder("bash", "-c", "pipreqs . --force");
                install = new ProcessBuilder("bash", "-c", "pip3 install cyclonedx-bom");
                generate = new ProcessBuilder("bash", "-c", "cyclonedx-py -i requirements.txt -o bom.xml");
                freeze.directory(new File(directory));
                Process freezeProcess = freeze.start();
                freezeProcess.waitFor();
                log.info("[Dependency Track] Freezing PIP dependencies for {}", directory);
                install.directory(new File(directory));
                installProcess = install.start();
                installProcess.waitFor();
                log.info("[Dependency Track] Installed CycloneDX PIP for {}", directory);
                generate.directory(new File(directory));
                generateProcess = generate.start();
                generateProcess.waitFor();
                log.info("[Dependency Track] Generated SBOM for {}", directory);
                return directory + File.separatorChar + "bom.xml";
            case NPM:
                install = new ProcessBuilder("bash", "-c", "npm install -g @cyclonedx/bom");
                install.directory(new File(directory));
                installProcess = install.start();
                installProcess.waitFor();
                log.info("[Dependency Track] Installed CycloneDX NPM for {}", directory);
                generate = new ProcessBuilder("bash", "-c", "cyclonedx-bom -o bom.xml");
                generate.directory(new File(directory));
                generateProcess = generate.start();
                generateProcess.waitFor();
                log.info("[Dependency Track] Generated SBOM for {}", directory);
                return directory + File.separatorChar + "bom.xml";
            case MAVEN:
                generate = new ProcessBuilder("bash", "-c", "mvn -DskipTests -DSPDXParser.OnlyUseLocalLicenses=true org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom").inheritIO();
                generate.directory(new File(directory));
                generateProcess = generate.start();
                generateProcess.waitFor();
                log.info("[Dependency Track] Generated SBOM for {}", directory);
                return directory + File.separatorChar + "target" + File.separatorChar + "bom.xml";
            case GRADLE:
                log.error("[Dependency Track] GRADLE not yet supported");
                break;
            case PHP:
                install = new ProcessBuilder("bash", "-c", "composer require --dev cyclonedx/cyclonedx-php-composer");
                install.directory(new File(directory));
                installProcess = install.start();
                installProcess.waitFor();
                log.info("[Dependency Track] Installed CycloneDX COMPOSER for {}", directory);
                generate = new ProcessBuilder("bash", "-c", "composer make-bom");
                generate.directory(new File(directory));
                generateProcess = generate.start();
                generateProcess.waitFor();
                log.info("[Dependency Track] Generated SBOM for {}", directory);
                return directory + File.separatorChar + "bom.xml";
            default:
                return null;
        }
        return null;
    }

    /**
     * Getting oAuth token for Dependency track using default username and password
     *
     * @return oAuth token
     */
    private String getOAuthToken(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(Constants.DEPENDENCYTRACK_LOGIN_STRING, headers);
        ResponseEntity<String> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                Constants.DEPENDENCYTRACK_URL_LOGIN, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            return response.getBody();
        }
        return null;
    }

    /**
     * Loading projects from DependencyTrack to check if project is already created on platform
     *
     * @return list of projects
     */
    private List<DTrackProject> getProjects(DependencyTrackEntity dependencyTrackEntity) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.DEPENDENCYTRACK_APIKEY_HEADER, dependencyTrackEntity.getApiKey());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<List<DTrackProject>> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                    Constants.DEPENDENCYTRACK_GET_PROJECTS, HttpMethod.GET, entity, new ParameterizedTypeReference<List<DTrackProject>>() {});
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("[Dependency Track] Unable to load Dependency Track projects");
            }
        } catch (HttpClientErrorException | HttpServerErrorException | ResourceAccessException e){
            log.error("[Dependency Track] Error during getting Dependency Track project list {}", e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Saving project to DependencyTrack
     *
     * @param dependencyTrackEntity needed for ApiKey
     * @param name name of project to be created
     * @return uuid of project
     */
    private String createProject(DependencyTrackEntity dependencyTrackEntity, String name, String branch, boolean standalone) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.DEPENDENCYTRACK_APIKEY_HEADER, dependencyTrackEntity.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DTrackCreateProject> entity = new HttpEntity<>(new DTrackCreateProject(standalone? UUID.randomUUID().toString() : name + "_" + branch),headers);
        try {
            ResponseEntity<DTrackCreateProjectResponse> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                    Constants.DEPENDENCYTRACK_GET_PROJECTS, HttpMethod.PUT, entity, DTrackCreateProjectResponse.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("[Dependency Track] Successfully created Dependency Track project for {} with UUID {}", name ,response.getBody().getUuid());
                return response.getBody().getUuid();
            } else {
                log.error("[Dependency Track] Unable to to create project Dependency Track for project {}", name);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e){
            log.error("[Dependency Track] Error during Creation of project for {} with code {}", name, e.getStatusCode());
        }
        return null;
    }

    /**
     * Change default admin password - DTrack require admin after first login to force password change
     *
     */
    private void changePassword() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(Constants.DEPENDENCYTRACK_CHANGE_PASSWORD_STRING, headers);
        ResponseEntity<String> response = restTemplate.exchange(Constants.DEPENDENCYTRACK_URL +
                Constants.DEPENDENCYTRACK_URL_CHANGE_PASSWORD, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().equals(HttpStatus.OK)){
            log.info("[Dependency Track] Default admin password changed");
        }
    }

    /**
     * Method which return UUID for project on DependencyTrack by ScanRequest.class.
     * If project is present it simply return its UUID, if project is not present it is being created
     *
     * @param dependencyTrackEntity entity for apiKey usage
     * @param scanRequest request with name and url repo
     * @return UUID on dependency track
     */
    private String getDTrackProjectUuid(DependencyTrackEntity dependencyTrackEntity, ScanRequest scanRequest, boolean standalone){
        List<DTrackProject> dTrackProjects = getProjects(dependencyTrackEntity);
        String projectName;
        if (standalone){
            projectName = UUID.randomUUID().toString();
        } else {
            projectName = CodeHelper.getNameFromRepoUrlforSAST(scanRequest.getTarget(), standalone) + "_" + scanRequest.getBranch();
        }
        if (dTrackProjects!= null && dTrackProjects.size() > 0) {
            Optional<DTrackProject> dTrackProject = dTrackProjects
                    .stream()
                    .filter(p -> p.getName().equals(projectName))
                    .findFirst();
            if (dTrackProject.isPresent()){
                return dTrackProject.get().getUuid();
            } else {
                return createProject(dependencyTrackEntity, CodeHelper.getNameFromRepoUrlforSAST(scanRequest.getTarget(), standalone), scanRequest.getBranch(), standalone);
            }
        } else {
            return createProject(dependencyTrackEntity, CodeHelper.getNameFromRepoUrlforSAST(scanRequest.getTarget(), standalone), scanRequest.getBranch(), standalone);
        }
    }

    /**
     * Running DTrack scan for given path, NPM only supported
     * @param packagePath path where package.json is located
     * @return
     */
    public List<Vulnerability> runScanStandalone(List<String> packagePath) throws Exception {
        this.prepare();
        List<Vulnerability> vulns = new ArrayList<>();
        List<String> uuids = new ArrayList<>();
        Optional<DependencyTrackEntity> dependencyTrack = dependencyTrackRepository.findByEnabledAndApiKeyNotNull(true);
        if(dependencyTrack.isPresent()) {
            for (String path : packagePath) {
                String uuid = getDTrackProjectUuid(dependencyTrack.get(), new ScanRequest(), true);
                //Only NPM supported ATM
                //SourceProjectType sourceProjectType = CodeHelper.getSourceProjectTypeFromDirectory(new ScanRequest(), true);

                log.info("[Dependency Track] Get UUID {} and type of project {}", uuid, SourceProjectType.NPM);
                String bomPath = generateBom(new ScanRequest(), SourceProjectType.NPM, true, path);
                if (bomPath == null) {
                    throw new Exception("SBOM path appears to be null");
                }
                sendBomToDTrack(dependencyTrack.get(), uuid, bomPath);
                uuids.add(uuid);
                log.info("[Dependency Track] Scan completed for {}", path);
            }
            //Sleep untill DTrack audit the bom
            TimeUnit.SECONDS.sleep(60);
            for (String uuid : uuids){
                vulns.addAll(convertDTrackResponseToVulnerabilities(loadVulnerabilities(dependencyTrack.get(), uuid)));
            }
            return vulns;

        } else {
            log.error("[Dependency Track] Trying to run scan on not properly initialized scanner. " +
                    "This should not happen, please collect log and issue ticket.");
        }
        return new ArrayList<>();
    }
}
