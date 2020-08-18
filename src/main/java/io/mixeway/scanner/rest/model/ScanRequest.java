/*
 * @created  2020-08-18 : 16:44
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.rest.model;

import io.mixeway.scanner.utils.ScannerPluginType;
import io.mixeway.scanner.utils.ScannerType;

public class ScanRequest {
    public ScannerType type;
    public String target;
    public String username;
    public String password;
    public String exclusions;

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
