package io.mixeway.scanner.utils;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gsiewruk
 */
@XmlRootElement(name = "project")
public class PomProject {
    PomBuild build;

    public PomBuild getBuild() {
        return build;
    }

    public void setBuild(PomBuild build) {
        this.build = build;
    }
}
