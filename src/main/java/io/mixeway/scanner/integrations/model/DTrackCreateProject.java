package io.mixeway.scanner.integrations.model;

public class DTrackCreateProject {
    private String name;

    public DTrackCreateProject(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
