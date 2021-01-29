package io.mixeway.scanner.integrations.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gsiewruk
 */
@XmlRootElement(name = "BugPattern")
@XmlAccessorType(XmlAccessType.FIELD)
public class BugPattern {
    @XmlElement(name = "ShortDescription")
    private String shortDescriptions;
    @XmlElement(name = "Details")
    private String details;


    public String getShortDescriptions() {
        return shortDescriptions;
    }

    public void setShortDescriptions(String shortDescriptions) {
        this.shortDescriptions = shortDescriptions;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
