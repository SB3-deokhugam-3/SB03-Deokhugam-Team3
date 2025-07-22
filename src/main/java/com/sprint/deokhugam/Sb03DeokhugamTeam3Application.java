package com.sprint.deokhugam;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication
public class Sb03DeokhugamTeam3Application {
    public static void main(String[] args) {
        SpringApplication.run(Sb03DeokhugamTeam3Application.class, args);
    }
}
