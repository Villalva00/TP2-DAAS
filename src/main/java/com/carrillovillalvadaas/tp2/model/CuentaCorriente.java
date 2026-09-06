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
@DiscriminatorValue("CUENTA_CORRIENTE")
public class CuentaCorriente extends CuentaFinanciera {

    @Column(name = "margen_descubierto_autorizado", nullable = false, precision = 15, scale = 2)
    private BigDecimal margenDescubiertoAutorizado;

    @Column(name = "costo_mantenimiento", nullable = false, precision = 15, scale = 2)
    private BigDecimal costoMantenimiento;

    public CuentaCorriente(String cbu, String alias, Cliente cliente,
                           BigDecimal margenDescubiertoAutorizado, BigDecimal costoMantenimiento) {
        super(cbu, alias, cliente);
        this.margenDescubiertoAutorizado = margenDescubiertoAutorizado;
        this.costoMantenimiento = costoMantenimiento;
    }
}