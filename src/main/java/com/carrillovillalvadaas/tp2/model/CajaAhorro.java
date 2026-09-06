package com.carrillovillalvadaas.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("CAJA_AHORRO")
public class CajaAhorro extends CuentaFinanciera {

    @Column(name = "tasa_interes_anual", nullable = false, precision = 8, scale = 4)
    private BigDecimal tasaInteresAnual;

    @Column(name = "limite_extraccion_mensual", nullable = false, precision = 15, scale = 2)
    private BigDecimal limiteExtraccionMensual;

    public CajaAhorro(String cbu, String alias, Cliente cliente,
                      BigDecimal tasaInteresAnual, BigDecimal limiteExtraccionMensual) {
        super(cbu, alias, cliente);
        this.tasaInteresAnual = tasaInteresAnual;
        this.limiteExtraccionMensual = limiteExtraccionMensual;
    }
}