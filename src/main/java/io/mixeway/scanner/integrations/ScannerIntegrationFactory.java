/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations;

import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.Vulnerability;

import java.util.List;

public interface ScannerIntegrationFactory {
    void prepare() throws Exception;
    List<Vulnerability> runScan(ScanRequest scanRequest) throws Exception;
    List<Vulnerability> runScanStandalone() throws Exception;
}
