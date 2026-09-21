package com.carrillovillalvadaas.tp2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una caja de ahorro dentro del sistema financiero.
 * Hereda de {@link CuentaFinanciera} e incorpora la gestión de tasa de interés anual
 * y límites de extracción específicos.
 *
 * @see CuentaFinanciera
 */


/**
 * Le indica a JPA que esta clase es una entidad persistente y que estará mapeada a una tabla en la base de datos.
 */
@Entity

/**
 * Define explícitamente el nombre que tendrá la tabla en la base de datos (caja_ahorro).
 */
@Table(name = "caja_ahorro")


@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor

public class CajaAhorro extends CuentaFinanciera {

    /**
     * Tasa de interés anual aplicable a la caja de ahorro.
     */
    @Column(name = "tasa_interes_anual")
    private Double tasaInteresAnual;

    /**
     * Límite máximo permitido por extracción para este tipo de cuenta.
     */
    @Column(name = "limite_extraccion")
    private Integer limiteExtraccion;



    /**
     * Calcula el interés generado en base al saldo operativo actual y la tasa de interés anual.
     *
     * @return El monto del interés calculado, o 0.0 si faltan datos.
     */
    public Double calcularInteres() {
        if (tasaInteresAnual == null || getSaldoOperativo() == null) {
            return 0.0;
        }
        return getSaldoOperativo() * (tasaInteresAnual / 100.0);
    }


    /**
     * Implementación del método abstracto de extracción para la caja de ahorro.
     * Permite retirar fondos siempre que el saldo operativo sea suficiente y el monto sea válido.
     *
     * @param monto Cantidad monetaria que se desea extraer.
     */
    @Override
    public void extraer(Double monto) {
        if (monto != null && monto > 0 && getSaldoOperativo() >= monto) {
            setSaldoOperativo(getSaldoOperativo() - monto);
        }
    }
}
