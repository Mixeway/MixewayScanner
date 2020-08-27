/*
 * @created  2020-08-27 : 16:53
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.utils;

import io.mixeway.scanner.integrations.model.DTrackVuln;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    public void sendRequestToMixeway(List<Vulnerability> vulnerabilities, String projectName, String branch, String commit) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (StringUtils.isNoneBlank(mixewayKey) && mixewayProject > 0){
            log.info("[Mixeway Connector] Mixeway integraiton is enabled. Starting to push the results to {}", mixewayUrl);
            RestTemplate restTemplate = getRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Vulnerability>> entity = new HttpEntity<>(vulnerabilities,headers);
            ResponseEntity<String> response = restTemplate.exchange(mixewayUrl +
                    Constants.MIXEWAY_PUSH_VULN_URL + "/" + mixewayProject + "/" + projectName + "/" + branch + "/" + commit,
                    HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[Mixeway Connector] Results pushed and already visible at {}", mixewayUrl);
            } else {
                log.error("[Mixeway Connector] Push results to Mixeway - {}", response.getStatusCodeValue());
            }
        } else {
            log.info("[Mixeway Connector] Mixeway integration is not enabled, if You want to push results into mixeway please set MIXEWAY_URL and MIXEWAY_KEY. Read more at docs.");
        }

    }

    public void sendAnonymousRequestToMixeway(List<Vulnerability> vulnerabilities) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (StringUtils.isNoneBlank(mixewayProjectName) && StringUtils.isNoneBlank(mixewayKey)){
            log.info("[Mixeway Connector] Mixeway integraiton is enabled. Starting to push the results to {}", mixewayUrl);
            RestTemplate restTemplate = getRestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set(Constants.MIXEWAY_API_KEY, mixewayKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Vulnerability>> entity = new HttpEntity<>(vulnerabilities, headers);
            ResponseEntity<String> response = restTemplate.exchange(mixewayUrl +
                            Constants.MIXEWAY_PUSH_VULN_URL + "/" + mixewayProjectName,
                    HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[Mixeway Connector] Results pushed and already visible at {}", mixewayUrl);
            } else {
                log.error("[Mixeway Connector] Push results to Mixeway - {}", response.getStatusCodeValue());
            }
        } else {
            log.info("[Mixeway Connector] Mixeway integration is not enabled, if You want to push results into mixeway please set MIXEWAY_URL, MIXEWAY_KEY and MIXEWAY_PROJECT_NAME. Read more at docs.");
        }

    }
    public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        };
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
}
