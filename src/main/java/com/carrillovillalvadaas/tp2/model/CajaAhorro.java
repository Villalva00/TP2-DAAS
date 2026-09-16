package com.carrillovillalvadaas.tp2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caja_ahorro")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CajaAhorro extends CuentaFinanciera {

    @Column(name = "tasa_interes_anual")
    private Double tasaInteresAnual;

    @Column(name = "limite_extraccion")
    private Integer limiteExtraccion;


    // Método de negocio de tu UML
    public Double calcularInteres() {
        if (tasaInteresAnual == null || getSaldoOperativo() == null) {
            return 0.0;
        }
        return getSaldoOperativo() * (tasaInteresAnual / 100.0);
    }

    @Override
    public void extraer(Double monto) {
        if (monto != null && monto > 0 && getSaldoOperativo() >= monto) {
            setSaldoOperativo(getSaldoOperativo() - monto);
        }
    }
}
