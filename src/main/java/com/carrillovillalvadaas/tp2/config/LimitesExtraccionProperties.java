package com.carrillovillalvadaas.tp2.config;

import com.carrillovillalvadaas.tp2.model.TipoCliente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Límites globales de extracción diaria, parametrizables desde application.properties.
 *
 * @param titular   Tope diario para clientes titulares.
 * @param adherente Tope diario para clientes adherentes.
 */
@Validated
@ConfigurationProperties(prefix = "app.limites.extraccion-diaria")
public record LimitesExtraccionProperties(
        @NotNull @Positive Double titular,
        @NotNull @Positive Double adherente) {

    /**
     * Devuelve el tope diario de extracción que corresponde al tipo de cliente.
     *
     * @param tipo Tipo de cliente (TITULAR o ADHERENTE).
     * @return El límite diario configurado para ese tipo.
     */
    public Double limiteParaTipo(TipoCliente tipo) {
        Objects.requireNonNull(tipo, "El tipo de cliente es obligatorio.");
        return switch (tipo) {
            case TITULAR -> titular;
            case ADHERENTE -> adherente;
        };
    }
}
