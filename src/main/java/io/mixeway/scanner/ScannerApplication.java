package io.mixeway.scanner;

import io.mixeway.scanner.factory.ScannerFactory;
import io.mixeway.scanner.integrations.ScannerIntegrationFactory;
import io.mixeway.scanner.utils.ScannerPluginType;
import io.mixeway.scanner.utils.ScannerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

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
