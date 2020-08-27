/*
 * @created  2020-08-27 : 16:53
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.utils;

import io.mixeway.scanner.integrations.model.DTrackVuln;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MixewayConnector {
    private final static Logger log = LoggerFactory.getLogger(MixewayConnector.class);
    @Value("${mixeway.url}")
    String mixewayUrl;
    @Value("${mixeway.key}")
    String mixewayKey;
    @Value("${mixeway.project}")
    int mixewayProject;
    @Value("${mixeway.project.name}")
    String mixewayProjectName;

    public void sendRequestToMixeway(List<Vulnerability> vulnerabilities, String projectName, String branch, String commit){
        if (StringUtils.isNoneBlank(mixewayKey) && mixewayProject > 0){
            log.info("[Mixeway Connector] Mixeway integraiton is enabled. Starting to push the results to {}", mixewayUrl);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(mixewayUrl +
                    Constants.MIXEWAY_PUSH_VULN_URL + "/" + mixewayProject + "/" + projectName + "/" + branch + "/" + commit,
                    HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[Mixeway Connector] Results pushed and already visible at {}", mixewayUrl);
            } else {
                log.error("[Mixeway Connector] Push results to Mixeway - {}", response.getStatusCodeValue());
            }
        } else {
            log.info("[Mixeway Connector] Mixeway integration is not enabled, if You want to push results into mixeway please set MIXEWAY_URL and MIXEWAY_KEY. Read more at docs.");
        }

    }

    public void sendAnonymousRequestToMixeway(List<Vulnerability> vulnerabilities){
        if (StringUtils.isNoneBlank(mixewayProjectName) && StringUtils.isNoneBlank(mixewayKey) && mixewayProject > 0){
            log.info("[Mixeway Connector] Mixeway integraiton is enabled. Starting to push the results to {}", mixewayUrl);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(mixewayUrl +
                            Constants.MIXEWAY_PUSH_VULN_URL + "/" + mixewayProjectName,
                    HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[Mixeway Connector] Results pushed and already visible at {}", mixewayUrl);
            } else {
                log.error("[Mixeway Connector] Push results to Mixeway - {}", response.getStatusCodeValue());
            }
        } else {
            log.info("[Mixeway Connector] Mixeway integration is not enabled, if You want to push results into mixeway please set MIXEWAY_URL, MIXEWAY_KEY and MIXEWAY_PROJECT_NAME. Read more at docs.");
        }

    }
}
