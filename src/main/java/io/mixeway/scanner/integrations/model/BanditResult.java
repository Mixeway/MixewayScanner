package io.mixeway.scanner.integrations.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author gsiewruk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BanditResult {
    private String line_number;
    private String filename;
    private String issue_text;
    private String test_name;
    private String more_info;
    private String issue_severity;
    private String code;

    public String getLine_number() {
        return line_number;
    }

    public void setLine_number(String line_number) {
        this.line_number = line_number;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getIssue_text() {
        return issue_text;
    }

    public void setIssue_text(String issue_text) {
        this.issue_text = issue_text;
    }

    public String getTest_name() {
        return test_name;
    }

    public void setTest_name(String test_name) {
        this.test_name = test_name;
    }

    public String getMore_info() {
        return more_info;
    }

    public void setMore_info(String more_info) {
        this.more_info = more_info;
    }

    public String getIssue_severity() {
        return issue_severity;
    }

    public void setIssue_severity(String issue_severity) {
        this.issue_severity = issue_severity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
