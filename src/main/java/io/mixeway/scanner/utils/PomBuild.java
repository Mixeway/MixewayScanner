package io.mixeway.scanner.utils;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sun.xml.txw2.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author gsiewruk
 */
@Builder
@AllArgsConstructor
@XmlRootElement(name = "build")
@XmlElement("build")
public class PomBuild {
    List<PomPlugin> plugins;

    public PomBuild(){}

    public List<PomPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PomPlugin> plugins) {
        this.plugins = plugins;
    }
}
