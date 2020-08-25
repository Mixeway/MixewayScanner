package io.mixeway.scanner.factory;

import io.mixeway.scanner.db.repository.DependencyTrackRepository;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.scanner.DependencyTrack;
import io.mixeway.scanner.integrations.scanner.Spotbug;
import io.mixeway.scanner.utils.ScannerPluginType;
import javafx.scene.effect.Light;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class ScannerFactory {
    private final DependencyTrack dependencyTrack;
    private final Spotbug spotbug;

    public ScannerFactory(DependencyTrack dependencyTrack, Spotbug spotbug){
        this.dependencyTrack = dependencyTrack;
        this.spotbug = spotbug;
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
        }
        return scanner;

    }
}
