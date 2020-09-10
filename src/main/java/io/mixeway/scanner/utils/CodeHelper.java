package io.mixeway.scanner.utils;

import io.mixeway.scanner.rest.model.ScanRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gsiewruk
 */
@Component
public class CodeHelper {
    public static String sourceLocation;

    @Value( "${sources.location}" )
    public void setSourceLocation(String sourcesPath) {
        sourceLocation = sourcesPath;
    }

    /**
     * Gettig repo name from URL
     * e.g: https://github.com/mixeway/mixewayhub.git should return mixewayhub
     *
     * @param repoUrl URL for repository
     * @return name of repository
     */
    public static String getNameFromRepoUrlforSAST(String repoUrl, boolean standalone){
        if (standalone) {
            return "standaloneApp";
        } else {
            String[] partsOfUrl = repoUrl.split("/");
            String repoName = partsOfUrl[partsOfUrl.length - 1];
            return repoName.split("\\.")[0];
        }
    }
    /**
     * Determine type of source code in given location - JAVA-MVN, JAVA-Gradle, NPM, PIP or PHP-COMPOSER
     *
     */
    public static SourceProjectType getSourceProjectTypeFromDirectory(ScanRequest scanRequest, boolean standalone){
        String projectLocation;
        if (standalone){
            projectLocation = Constants.STANDALONE_DEFAULT_SOURCE_PATH;
        } else {
            projectLocation = sourceLocation + File.separatorChar + getNameFromRepoUrlforSAST(scanRequest.getTarget(), standalone);
        }
        File pom = new File(projectLocation + File.separatorChar + "pom.xml");
        if(pom.exists()){
            return SourceProjectType.MAVEN;
        }
        File gradle = new File(projectLocation + File.separatorChar + "build.xml");
        File gradle2 = new File(projectLocation + File.separatorChar + "build.gradle");
        if (gradle.exists() || gradle2.exists()) {
            prepareGradle(gradle);
            return SourceProjectType.GRADLE;
        }
        File npm = new File(projectLocation + File.separatorChar + "package.json");
        if(npm.exists()){
            return SourceProjectType.NPM;
        }
        File composer = new File(projectLocation + File.separatorChar + "composer.json");
        if(composer.exists() || directoryContainsPhp(projectLocation)){
            return SourceProjectType.PHP;
        }
        Collection pip = FileUtils.listFiles(
                new File(projectLocation),
                new RegexFileFilter(".*\\.py"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        if (pip.size() > 3) {
            return SourceProjectType.PIP;
        }
        return null;
    }
    /**
     * Check if project is is particular language
     */
    public static boolean isProjectInLanguage(String directory, SourceProjectType sourceProjectType){
        switch (sourceProjectType) {
            case PIP:
                Collection pip = FileUtils.listFiles(
                        new File(directory),
                        new RegexFileFilter(".*\\.py"),
                        DirectoryFileFilter.DIRECTORY
                ).stream().map(File::getName).collect(Collectors.toList());
                return pip.size() > 3;
            case GRADLE:
                File gradle = new File(directory + File.separatorChar + "build.xml");
                File gradle2 = new File(directory + File.separatorChar + "build.gradle");
                if (gradle.exists() || gradle2.exists()) {
                    prepareGradle(gradle);
                    return true;
                } else {
                    return false;
                }
            case PHP:
                File composer = new File(directory + File.separatorChar + "composer.json");
                return composer.exists() || directoryContainsPhp(directory);
            case NPM:
                List<String> packagePaths= FileUtils.listFiles(
                        new File(directory),
                        new RegexFileFilter(Constants.PACKAGE_FILENAME),
                        DirectoryFileFilter.DIRECTORY
                ).stream()
                        .map(File::getAbsoluteFile)
                        .map(file -> file.toString()
                                .split(File.separatorChar + Constants.PACKAGE_FILENAME)[0])
                        .collect(Collectors.toList());
                return packagePaths.size()>0;
            case MAVEN:
                File pom = new File(directory + File.separatorChar + "pom.xml");
                return pom.exists();
        }
        return false;
    }



    /**
     * Check if directory contains php files
     * @param projectLocation
     * @return
     */
    private static boolean directoryContainsPhp(String projectLocation) {
        Collection php = FileUtils.listFiles(
                new File(projectLocation),
                new RegexFileFilter(".*\\.php"),
                DirectoryFileFilter.DIRECTORY
        ).stream().map(File::getName).collect(Collectors.toList());
        return php.size() > 5;
    }

    /**
     * Method which preare build.xml file to use CycloneDX plugin to generate SBOM
     *
     * @param gradle path to gradle file
     */
    private static void prepareGradle(File gradle) {
        //TODO edit of build.xml
    }

    /**
     * Method which return path to project git downloaded
     *
     * @param scanRequest to process
     * @return path to code
     */
    public static String getProjectPath(ScanRequest scanRequest, boolean standalone){
        if (standalone){
            return Constants.STANDALONE_DEFAULT_SOURCE_PATH;
        } else {
            return  sourceLocation + File.separatorChar + getNameFromRepoUrlforSAST(scanRequest.getTarget(), standalone);
        }
    }
}
