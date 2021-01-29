package io.mixeway.scanner.factory;

import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.integrations.scanner.Bandit;
import io.mixeway.scanner.integrations.scanner.DependencyTrack;
import io.mixeway.scanner.integrations.scanner.Progpilot;
import io.mixeway.scanner.integrations.scanner.Spotbug;
import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScannerFactory {
    private final DependencyTrack dependencyTrack;
    private final Spotbug spotbug;
    private final Bandit bandit;
    private final Progpilot progpilot;

    public ScannerFactory(DependencyTrack dependencyTrack, Spotbug spotbug,
                          Bandit bandit, Progpilot progpilot){
        this.dependencyTrack = dependencyTrack;
        this.spotbug = spotbug;
        this.bandit = bandit;
        this.progpilot = progpilot;
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
            case PROGPILOT:
                scanner = progpilot;
                break;
        }
        return scanner;
    }

    public List<Vulnerability> runScanForLanguage(SourceProjectType sourceProjectType) throws Exception {
        List<Vulnerability> vulnerabilityList = new ArrayList<>();
        switch (sourceProjectType) {
            case NPM:
                List<String> packagePaths= FileUtils.listFiles(
                        new File(CodeHelper.getProjectPath(new ScanRequest(), true)),
                        new RegexFileFilter(Constants.PACKAGE_FILENAME),
                        DirectoryFileFilter.DIRECTORY
                ).stream()
                        .map(File::getAbsoluteFile)
                        .map(file -> file.toString()
                                .split(File.separatorChar + Constants.PACKAGE_FILENAME)[0])
                        .collect(Collectors.toList());
                vulnerabilityList.addAll(dependencyTrack.runScanStandalone(packagePaths));
                break;
            case PHP:
                vulnerabilityList.addAll(dependencyTrack.runScanStandalone());
                vulnerabilityList.addAll(progpilot.runScanStandalone());
                break;
            case PIP:
                vulnerabilityList.addAll(dependencyTrack.runScanStandalone());
                vulnerabilityList.addAll(bandit.runScanStandalone());
                break;
            case MAVEN:
                List<String> mvnPackagePaths= FileUtils.listFiles(
                        new File(CodeHelper.getProjectPath(new ScanRequest(), true)),
                        new RegexFileFilter(Constants.POM_FILENAME),
                        DirectoryFileFilter.DIRECTORY
                ).stream()
                        .map(File::getAbsoluteFile)
                        .map(file -> file.toString()
                                .split(File.separatorChar + Constants.POM_FILENAME)[0])
                        .collect(Collectors.toList());
                vulnerabilityList.addAll(dependencyTrack.runScanStandalone());
                for (String mvnPath : mvnPackagePaths) {
                    vulnerabilityList.addAll(spotbug.runScanStandalone(mvnPath)
                            .stream()
                            .filter(vulnerability -> vulnerability.getCategory().equals(Constants.SPOTBUG_CATEGORY_SECURITY) ||
                                    vulnerability.getCategory().equals(Constants.SPOTBUG_CATEGORY_MALICIOUS_CODE))
                    .collect(Collectors.toList()));
                }
                break;
            case GRADLE:
                break;
        }
        return vulnerabilityList.stream().distinct().collect(Collectors.toList());
    }
}
