/*
 * @created  2020-08-18 : 22:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations.scanner;


import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.rest.model.ScanRequest;
import org.springframework.stereotype.Component;

@Component
public class Spotbug implements ScannerIntegrationFactory {
    @Override
    public void prepare() {

    }

    @Override
    public void runScan(ScanRequest scanRequest) {

    }
}
