package com.carrillovillalvadaas.tp2.config;

import com.carrillovillalvadaas.tp2.model.TipoCliente;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica que los límites de extracción diaria se carguen desde las propiedades
 * y que el resolver devuelva el tope correcto según el tipo de cliente.
 */
class LimitesExtraccionPropertiesTest {

    @EnableConfigurationProperties(LimitesExtraccionProperties.class)
    static class TestConfig {
    }

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

    @Test
    void deberiaCargarLosValoresDesdeLasPropiedades() {
        contextRunner
                .withPropertyValues(
                        "app.limites.extraccion-diaria.titular=100000.00",
                        "app.limites.extraccion-diaria.adherente=70000.00")
                .run(context -> {
                    LimitesExtraccionProperties limites = context.getBean(LimitesExtraccionProperties.class);

                    assertThat(limites.titular()).isEqualTo(100000.00);
                    assertThat(limites.adherente()).isEqualTo(70000.00);
                });
    }

    @Test
    void limiteParaTipo_deberiaDevolverElTopeSegunElTipoDeCliente() {
        contextRunner
                .withPropertyValues(
                        "app.limites.extraccion-diaria.titular=100000.00",
                        "app.limites.extraccion-diaria.adherente=70000.00")
                .run(context -> {
                    LimitesExtraccionProperties limites = context.getBean(LimitesExtraccionProperties.class);

                    assertThat(limites.limiteParaTipo(TipoCliente.TITULAR)).isEqualTo(100000.00);
                    assertThat(limites.limiteParaTipo(TipoCliente.ADHERENTE)).isEqualTo(70000.00);
                });
    }

    @Test
    void limiteParaTipo_deberiaLanzarExcepcion_cuandoElTipoEsNulo() {
        LimitesExtraccionProperties limites = new LimitesExtraccionProperties(100000.00, 70000.00);

        assertThatThrownBy(() -> limites.limiteParaTipo(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deberiaFallarElArranque_cuandoFaltaUnaPropiedad() {
        contextRunner
                .withPropertyValues("app.limites.extraccion-diaria.titular=100000.00")
                .run(context -> assertThat(context).hasFailed());
    }
}