package com.example.alumni;

import com.example.alumni.config.PhantomBusterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PhantomBusterProperties.class)
public class AlumniApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlumniApplication.class, args);
    }
}
