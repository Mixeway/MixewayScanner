/*
 * @created  2020-08-18 : 18:19
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.integrations.model;

import java.util.List;

public class DependencyTrackConfiguration {
    String uuid;
    String name;
    List<DTrackApiKeys> apiKeys;

    public List<DTrackApiKeys> getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(List<DTrackApiKeys> apiKeys) {
        this.apiKeys = apiKeys;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
