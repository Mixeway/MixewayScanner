/*
 * @created  2020-08-18 : 23:34
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.rest.service;

import io.mixeway.scanner.factory.ScannerFactory;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.scanner.DependencyTrack;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.rest.model.Status;
import io.mixeway.scanner.utils.GitOperations;
import io.mixeway.scanner.utils.GitResponse;
import io.mixeway.scanner.utils.ScannerPluginType;
import io.mixeway.scanner.utils.ScannerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BaseService {
    private final static Logger log = LoggerFactory.getLogger(BaseService.class);
    ScannerFactory scannerFactory;
    GitOperations gitOperations;

    public BaseService(ScannerFactory scannerFactory, GitOperations gitOperations){
        this.scannerFactory = scannerFactory;
        this.gitOperations = gitOperations;
    }

    /**
     * Running scan for given request
     *
     * @param scanRequest sent by client
     * @return status
     */
    public Status runScan(ScanRequest scanRequest) {
        try {
            if (scanRequest.getType().equals(ScannerType.SAST)) {
                // TODO get opensource scan
                if (gitOperations.isProjectPresent(scanRequest)) {
                    GitResponse gitResponse = gitOperations.pull(scanRequest);
                } else {
                    GitResponse gitResponse = gitOperations.clone(scanRequest);
                }
                ScannerIntegrationFactory scannerIntegrationFactory = scannerFactory.getProperScanner(ScannerPluginType.DEPENDENCYTRACK);
                scannerIntegrationFactory.runScan(scanRequest);
                // TODO get SAST SCAN
            } else if (scanRequest.getType().equals(ScannerType.DAST)) {
                // TODO run DAST SCAN
            } else {
                log.error("[REST] Got request with unknown scan type {}", scanRequest.getType().toString());
            }
        } catch (Exception e){
            log.error(e.getLocalizedMessage());
        }
        return null;
    }
}
