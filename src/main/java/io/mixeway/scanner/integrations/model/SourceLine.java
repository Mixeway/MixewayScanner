package io.mixeway.scanner.integrations.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gsiewruk
 */
@XmlRootElement(name = "SourceLine")
@XmlAccessorType(XmlAccessType.FIELD)
public class SourceLine {
    @XmlAttribute
    private String start;
    @XmlAttribute
    private String end;
    @XmlAttribute
    private String sourcefile;
    @XmlAttribute
    private String classname;
    @XmlAttribute
    private String sourcepath;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getSourcefile() {
        return sourcefile;
    }

    public void setSourcefile(String sourcefile) {
        this.sourcefile = sourcefile;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getSourcepath() {
        return sourcepath;
    }

    public void setSourcepath(String sourcepath) {
        this.sourcepath = sourcepath;
    }
}
