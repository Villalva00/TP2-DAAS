package com.carrillovillalvadaas.tp2.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que las comisiones globales de mantenimiento se carguen desde las
 * propiedades {@code app.comisiones.*} y que el binding falle si falta alguna.
 */
class ComisionesPropertiesTest {

    @EnableConfigurationProperties(ComisionesProperties.class)
    static class TestConfig {
    }

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

    @Test
    void deberiaCargarLosValoresDesdeLasPropiedades() {
        contextRunner
                .withPropertyValues(
                        "app.comisiones.cuenta-corriente=5000.00",
                        "app.comisiones.caja-ahorro=2000.00")
                .run(context -> {
                    ComisionesProperties comisiones = context.getBean(ComisionesProperties.class);

                    assertThat(comisiones.cuentaCorriente()).isEqualTo(5000.00);
                    assertThat(comisiones.cajaAhorro()).isEqualTo(2000.00);
                });
    }

    @Test
    void deberiaFallarElArranque_cuandoFaltaUnaComision() {
        contextRunner
                .withPropertyValues("app.comisiones.cuenta-corriente=5000.00")
                .run(context -> assertThat(context).hasFailed());
    }
}