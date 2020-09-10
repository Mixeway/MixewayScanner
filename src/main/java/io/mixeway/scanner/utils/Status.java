package io.mixeway.scanner.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Status {
    private final static Logger log = LoggerFactory.getLogger(Status.class);
    private String status;
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Status(){}

    public Status(String status) {
        this.status = status;
    }

    public Status(String status, String requestId) {
        this.status = status;
        this.requestId = requestId;
    }

}
