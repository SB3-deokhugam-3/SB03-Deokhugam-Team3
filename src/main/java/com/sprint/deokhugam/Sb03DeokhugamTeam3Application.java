package com.sprint.deokhugam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Sb03DeokhugamTeam3Application {

    public static void main(String[] args) {
        SpringApplication.run(Sb03DeokhugamTeam3Application.class, args);
    }

}
