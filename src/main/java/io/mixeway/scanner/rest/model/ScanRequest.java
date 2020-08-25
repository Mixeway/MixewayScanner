/*
 * @created  2020-08-18 : 16:44
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.rest.model;

import io.mixeway.scanner.utils.ScannerPluginType;
import io.mixeway.scanner.utils.ScannerType;

import javax.validation.constraints.NotNull;

public class ScanRequest {
    @NotNull(message = "Scanner Type cannot be null possible options 'SAST' or 'DAST'")
    public ScannerType type;
    @NotNull(message = "Target cannot be null. In scope of SAST scan target is GIT repo, in scope of DAST scan target is URL to scan")
    public String target;
    public String username;
    public String password;
    public String exclusions;
    public String branch;
    public String projectName;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public ScannerType getType() {
        return type;
    }

    public void setType(ScannerType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExclusions() {
        return exclusions;
    }

    public void setExclusions(String exclusions) {
        this.exclusions = exclusions;
    }
}
