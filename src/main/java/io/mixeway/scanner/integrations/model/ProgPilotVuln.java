package io.mixeway.scanner.integrations.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author gsiewruk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgPilotVuln {
    private int[] source_line;
    private String[] source_file;
    private String vuln_name;
    private String vuln_cwe;



    public String getVuln_name() {
        return vuln_name;
    }

    public void setVuln_name(String vuln_name) {
        this.vuln_name = vuln_name;
    }

    public String getVuln_cwe() {
        return vuln_cwe;
    }

    public void setVuln_cwe(String vuln_cwe) {
        this.vuln_cwe = vuln_cwe;
    }

    public int[] getSource_line() {
        return source_line;
    }

    public void setSource_line(int[] source_line) {
        this.source_line = source_line;
    }

    public String[] getSource_file() {
        return source_file;
    }

    public void setSource_file(String[] source_file) {
        this.source_file = source_file;
    }
}
