package io.mixeway.scanner.utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author gsiewruk
 */
@XmlRootElement(name = "plugins")
public class PomPlugins {
    List<PomPlugin> plugins;

    public List<PomPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PomPlugin> plugins) {
        this.plugins = plugins;
    }
}
