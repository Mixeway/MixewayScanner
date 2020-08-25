package io.mixeway.scanner.factory;

import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.scanner.Bandit;
import io.mixeway.scanner.integrations.scanner.DependencyTrack;
import io.mixeway.scanner.integrations.scanner.Spotbug;
import io.mixeway.scanner.utils.ScannerPluginType;
import org.springframework.stereotype.Service;

@Service
public class ScannerFactory {
    private final DependencyTrack dependencyTrack;
    private final Spotbug spotbug;
    private final Bandit bandit;

    public ScannerFactory(DependencyTrack dependencyTrack, Spotbug spotbug,
                          Bandit bandit){
        this.dependencyTrack = dependencyTrack;
        this.spotbug = spotbug;
        this.bandit = bandit;
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
                scanner = spotbug;
                break;
            case BANDIT:
                scanner = bandit;
                break;
        }
        return scanner;

    }
}
