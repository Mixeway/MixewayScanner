/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations;

import io.mixeway.scanner.rest.model.ScanRequest;

public interface ScannerIntegrationFactory {
    void prepare() throws Exception;
    void runScan(ScanRequest scanRequest) throws Exception;
    void runScanStandalone() throws Exception;
}
