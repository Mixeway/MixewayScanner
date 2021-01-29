package io.mixeway.scanner.integrations.model;

import javax.xml.bind.annotation.*;

/**
 * @author gsiewruk
 */
@XmlRootElement(name = "BugInstance")
@XmlAccessorType(XmlAccessType.FIELD)
public class BugInstance {
    @XmlAttribute
    private String cweid;
    @XmlAttribute
    private String rank;
    @XmlAttribute
    private String abbrev;
    @XmlAttribute
    private String category;
    @XmlAttribute
    private String priority;
    @XmlAttribute
    private String type;
    @XmlElement(name = "LongMessage")
    private String longMessage;
    @XmlElement(name = "ShortMessage")
    private String shortDescription;
    @XmlElement(name= "SourceLine")
    private SourceLine sourceLine;
    @XmlElement(name= "Details")
    private String details;

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public SourceLine getSourceLine() {
        return sourceLine;
    }

    public void setSourceLine(SourceLine sourceLine) {
        this.sourceLine = sourceLine;
    }

    public String getCweid() {
        return cweid;
    }

    public void setCweid(String cweid) {
        this.cweid = cweid;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLongMessage() {
        return longMessage;
    }

    public void setLongMessage(String longMessage) {
        this.longMessage = longMessage;
    }
}
