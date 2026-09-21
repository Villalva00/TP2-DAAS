package com.carrillovillalvadaas.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una cuenta corriente dentro del sistema financiero.
 * Hereda de {@link CuentaFinanciera} y permite operar con un margen de descubierto autorizado
 * y un costo por comisión de mantenimiento asociado.
 *
 * @see CuentaFinanciera
 */



@Entity
@Table(name = "cuenta_corriente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CuentaCorriente extends CuentaFinanciera {

    /**
     * Margen o límite máximo de descubierto autorizado para realizar extracciones.
     */
    @Column(name = "margen_descubierto_autorizado")
    private Double margenDescubiertoAutorizado;


    /**
     * Costo correspondiente a la comisión de mantenimiento de la cuenta corriente.
     */
    @Column(name = "costo_comision_mantenimiento")
    private Double costoComisionMantenimiento;

    /**
     * Implementación del método abstracto de extracción.
     * Permite retirar fondos considerando el saldo operativo actual sumado al margen de descubierto autorizado.
     *
     * @param monto Cantidad monetaria que se desea extraer.
     */
    @Override
    public void extraer(Double monto) {
        if (monto == null || monto <= 0) return;

        double limiteTotal = getSaldoOperativo() + (margenDescubiertoAutorizado != null ? margenDescubiertoAutorizado : 0.0);

        if (limiteTotal >= monto) {
            setSaldoOperativo(getSaldoOperativo() - monto);
        }
    }
}