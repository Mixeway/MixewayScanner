package io.mixeway.scanner.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.xml.txw2.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gsiewruk
 */
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@XmlRootElement(name = "plugin")
@XmlElement("build")
public class PomPlugin {
    String groupId;
    String artifactId;
    String version;
    PomConfiguration configuration;

    public PomPlugin(){}

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public PomConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(PomConfiguration configuration) {
        this.configuration = configuration;
    }
}
