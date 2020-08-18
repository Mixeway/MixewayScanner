package io.mixeway.scanner.factory;

import io.mixeway.scanner.db.repository.DependencyTrackRepository;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.scanner.DependencyTrack;
import io.mixeway.scanner.integrations.scanner.Spotbug;
import io.mixeway.scanner.utils.ScannerPluginType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class ScannerFactory {
    private final DependencyTrack dependencyTrack;

    public ScannerFactory(DependencyTrack dependencyTrack){
        this.dependencyTrack = dependencyTrack;
    }

    /**
     * Method which takes proper vulnerability scanner based on a type
     *
     * @param type type of a scanner
     * @return returning factory of given type
     */
    public ScannerIntegrationFactory getProperScanner(ScannerPluginType type) {
        ScannerIntegrationFactory scanner = null;
        switch (type) {
            case DEPENDENCYTRACK:
                scanner = dependencyTrack;
                break;
            case SPOTBUG:
                scanner = new Spotbug();
                break;
        }
        return scanner;

    }
}
