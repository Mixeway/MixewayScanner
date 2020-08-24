package io.mixeway.scanner.integrations.model;

/**
 * @author gsiewruk
 */
public class SendBomRequest {

    private String project;
    private String bom;


    public SendBomRequest(String project, String bom) {
        this.project = project;
        this.bom = bom;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getBom() {
        return bom;
    }

    public void setBom(String bom) {
        this.bom = bom;
    }
}
