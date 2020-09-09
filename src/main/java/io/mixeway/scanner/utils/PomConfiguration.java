package io.mixeway.scanner.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author gsiewruk
 */
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@XmlRootElement(name = "configuration")
public class PomConfiguration {
    String effort;
    String threshold;
    String failOnError;
    List<PomPlugin> plugins;

    public PomConfiguration(){}


    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getEffort() {
        return effort;
    }

    public void setEffort(String effort) {
        this.effort = effort;
    }

    public String getFailOnError() {
        return failOnError;
    }

    public void setFailOnError(String failOnError) {
        this.failOnError = failOnError;
    }

    public List<PomPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PomPlugin> plugins) {
        this.plugins = plugins;
    }
}
