package io.mixeway.scanner.integrations.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author gsiewruk
 */
@XmlRootElement(name = "BugCollection")
@XmlAccessorType(XmlAccessType.FIELD)
public class SpotbugReportXML {
    @XmlElement(name="BugInstance")
    List<BugInstance> bugInstanceList;
    @XmlElement(name="BugPattern")
    List<BugPattern> bugPatterns;

    public List<BugPattern> getBugPatterns() {
        return bugPatterns;
    }

    public void setBugPatterns(List<BugPattern> bugPatterns) {
        this.bugPatterns = bugPatterns;
    }

    public List<BugInstance> getBugInstanceList() {
        return bugInstanceList;
    }

    public void setBugInstanceList(List<BugInstance> bugInstanceList) {
        this.bugInstanceList = bugInstanceList;
    }

    public SpotbugReportXML processSeverity() {
        if (this.bugInstanceList != null) {
            this.bugInstanceList.stream().filter(bi -> bi.getPriority() == null).forEach(info -> info.setPriority("Info"));
            this.bugInstanceList.stream().filter(bi -> bi.getPriority().equals("1")).forEach(high -> high.setPriority("High"));
            this.bugInstanceList.stream().filter(bi -> bi.getPriority().equals("2")).forEach(high -> high.setPriority("Medium"));
            this.bugInstanceList.stream().filter(bi -> bi.getPriority().equals("3")).forEach(high -> high.setPriority("Low"));
            this.bugInstanceList.stream().filter(bi -> bi.getPriority().equals("4")).forEach(high -> high.setPriority("Info"));
        }
        return this;
    }
}
