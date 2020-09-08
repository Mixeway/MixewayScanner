package io.mixeway.scanner.utils;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author gsiewruk
 */
public class Pom {
    @XmlElement
    PomBuild build;

    public PomBuild getBuild() {
        return build;
    }

    public void setBuild(PomBuild build) {
        this.build = build;
    }
}
