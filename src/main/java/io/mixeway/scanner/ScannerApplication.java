package io.mixeway.scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mixeway.scanner.factory.ScannerFactory;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.standalone.StandAloneService;
import io.mixeway.scanner.utils.ScannerPluginType;
import io.mixeway.scanner.utils.ScannerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication(scanBasePackages = "io.mixeway.scanner")
@EnableScheduling
@EnableJpaRepositories("io.mixeway.scanner.db.repository")
@EntityScan(basePackages = "io.mixeway.scanner.db.entity")
@EnableJpaAuditing
public class ScannerApplication {

   public static void main(String[] args) {

       SpringApplication.run(ScannerApplication.class, args);
    }
}

@Service
@ConditionalOnNotWebApplication
class StandaloneMixewayApp {
    private final StandAloneService standAloneService;

    public StandaloneMixewayApp(StandAloneService standAloneService){
        this.standAloneService = standAloneService;
    }
    @EventListener(ApplicationReadyEvent.class)
    public void runStandaloneMixewayScannerApp() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
        standAloneService.runScan();
    }
}
