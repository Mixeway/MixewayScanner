/*
 * @created  2020-08-18 : 16:43
 * @project  MixewayScanner
 * @author   siewer
 */
package io.mixeway.scanner.rest.controller;

import io.mixeway.scanner.rest.model.ScanRequest;
import io.mixeway.scanner.rest.model.Status;
import io.mixeway.scanner.rest.service.BaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class BaseController {
    BaseService baseService;

    public BaseController(BaseService baseService){
        this.baseService = baseService;
    }
    @PostMapping("/run")
    public ResponseEntity<Status> runScan(@Valid @RequestBody ScanRequest scanRequest) throws Exception {
        return baseService.runScan(scanRequest);
    }
}
