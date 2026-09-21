package com.carrillovillalvadaas.tp2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal que actúa como punto de entrada de la aplicación Spring Boot
 * para el sistema bancario.
 *
 * Habilita el mecanismo de auditoría de JPA mediante la anotación {@link EnableJpaAuditing}
 * para el registro automático de fechas de creación y modificación en las entidades.
 */
@SpringBootApplication
@EnableJpaAuditing
public class Tp2Application {

    /**
     * Método principal que inicia y ejecuta el contenedor de Spring Boot.
     *
     * @param args Argumentos de la línea de comandos pasados al ejecutar la aplicación.
     */
    public static void main(String[] args) {
        SpringApplication.run(Tp2Application.class, args);
    }
}