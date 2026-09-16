package com.carrillovillalvadaas.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name =  "cuenta_corriente")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class CuentaCorriente extends CuentaFinanciera {

    @Column(name = "margen_descubierto_autorizado")
    private Double margenDescubiertoAutorizado;

    @Column(name = "costo_comision_mantenimiento")
    private Double costoComisionMantenimiento;




    @Override
    public void extraer(Double monto) {
        if (monto == null || monto <= 0) return;
        double limiteTotal = getSaldoOperativo() + (margenDescubiertoAutorizado != null ? margenDescubiertoAutorizado : 0.0);
        if (limiteTotal >= monto) {
            setSaldoOperativo(getSaldoOperativo() - monto);
        }
    }
}
