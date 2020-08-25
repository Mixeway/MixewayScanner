/*
 * @created  2020-08-18 : 23:35
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.rest.model;

public class Status {
    String result;

    public Status(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
