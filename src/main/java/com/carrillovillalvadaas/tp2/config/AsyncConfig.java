package com.carrillovillalvadaas.tp2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración de la ejecución asíncrona de tareas de la aplicación.
 * <p>
 * Habilita {@link EnableAsync} para que los métodos anotados con
 * {@code @Async} (por ejemplo, el envío de emails) se ejecuten en un hilo
 * separado, de modo que el request HTTP del alta no espere al SMTP.
 * </p>
 * <p>
 * Se define un {@link ThreadPoolTaskExecutor} acotado: si el pool y la cola se
 * saturan, las tareas se ejecutan en el hilo que las invoca (política de
 * rechazo por defecto), evitando perder eventos de forma silenciosa.
 * </p>
 *
 * @author Villalva Elias Maciel, Carrillo Gonzalo Alejo
 *         Desarrollo y Arquitecturas Avanzadas de Software (UNJu)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Pool de hilos dedicado a las tareas asíncronas (envío de correo).
     * El nombre {@code taskExecutor} es el que Spring resuelve por defecto
     * cuando hay un único {@link Executor} disponible.
     *
     * @return el ejecutor configurado para las tareas de {@code @Async}.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
