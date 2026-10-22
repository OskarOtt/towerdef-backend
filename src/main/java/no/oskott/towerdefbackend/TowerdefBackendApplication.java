package no.oskott.towerdefbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TowerdefBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TowerdefBackendApplication.class, args);
    }

}
