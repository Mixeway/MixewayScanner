package io.mixeway.scanner.integrations.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author gsiewruk
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BanditResponse {
    List<BanditResult> results;

    public List<BanditResult> getResults() {
        return results;
    }

    public void setResults(List<BanditResult> results) {
        this.results = results;
    }
}
