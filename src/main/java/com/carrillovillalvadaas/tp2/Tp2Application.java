package com.carrillovillalvadaas.tp2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication


@EnableJpaAuditing
//  activa el sistema de auditoría

public class Tp2Application {

    static void main(String[] args) {
        SpringApplication.run(Tp2Application.class, args);
    }

}