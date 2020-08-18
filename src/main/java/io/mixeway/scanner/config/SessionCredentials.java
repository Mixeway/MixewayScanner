/*
 * @created  2020-08-18 : 21:39
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.config;

public class SessionCredentials {

    String apiKey;
    String accessToken;

    public SessionCredentials(String apiKey, String accessToken) {
        this.apiKey = apiKey;
        this.accessToken = accessToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAccessToken() {
        return accessToken;
    }
}