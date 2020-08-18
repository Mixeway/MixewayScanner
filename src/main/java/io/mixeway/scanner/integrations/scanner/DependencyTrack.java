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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DependencyTrack implements ScannerIntegrationFactory {
    private final static Logger log = LoggerFactory.getLogger(DependencyTrack.class);
    private DependencyTrackRepository dependencyTrackRepository;
    @Value("${OSS_USERNAME:blank}")
    private String ossUsername;
    @Value("${OSS_KEY:blank}")
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
            getApiKey();
            setOssIntegration();
        }
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
    }

    @Override
    public void runScan(ScanRequest scanRequest) throws Exception {
        this.prepare();
        Optional<DependencyTrackEntity> dependencyTrack = dependencyTrackRepository.findByEnabledAndApiKeyNotNull(true);
        if(dependencyTrack.isPresent()) {
            // TODO get uuid of project (create or get from list)
            String uuid = getDTrackProjectUuid(dependencyTrack.get(),scanRequest);
            // TODO Clone repository
            // TODO determine project language
            // TODO prepare project to generate BOM
            // TODO generate bom
            // TODO send bom to dependencytrack
            // TODO Create scan entity
            // TODO get results
            // TODO update scan entity
        } else {
            log.error("[Dependency Track] Trying to run scan on not properly initialized scanner. " +
                    "This should not happen, please collect log and issue ticket.");
        }

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
     * Gettig repo name from URL
     * e.g: https://github.com/mixeway/mixewayhub.git should return mixewayhub
     *
     * @param repoUrl URL for repository
     * @return name of repository
     */
    private String getNameFromRepoUrl(String repoUrl){
        String[] partsOfUrl = repoUrl.split("/");
        String repoName = partsOfUrl[partsOfUrl.length-1];
        return repoName.split("\\.")[0];
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
    private String createProject(DependencyTrackEntity dependencyTrackEntity, String name) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.DEPENDENCYTRACK_APIKEY_HEADER, dependencyTrackEntity.getApiKey());
        HttpEntity<DTrackCreateProject> entity = new HttpEntity<>(new DTrackCreateProject(name),headers);
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
     * Method which return UUID for project on DependencyTrack by ScanRequest.class.
     * If project is present it simply return its UUID, if project is not present it is being created
     *
     * @param dependencyTrackEntity entity for apiKey usage
     * @param scanRequest request with name and url repo
     * @return UUID on dependency track
     */
    private String getDTrackProjectUuid(DependencyTrackEntity dependencyTrackEntity, ScanRequest scanRequest){
        List<DTrackProject> dTrackProjects = getProjects(dependencyTrackEntity);
        if (dTrackProjects!= null && dTrackProjects.size() > 0) {
            Optional<DTrackProject> dTrackProject = dTrackProjects.stream().filter(p -> p.getName().equals(getNameFromRepoUrl(scanRequest.getTarget()))).findFirst();
            if (dTrackProject.isPresent()){
                return dTrackProject.get().getUuid();
            } else {
                return createProject(dependencyTrackEntity, getNameFromRepoUrl(scanRequest.getTarget()));
            }
        } else {
            return createProject(dependencyTrackEntity, getNameFromRepoUrl(scanRequest.getTarget()));
        }
    }
}
