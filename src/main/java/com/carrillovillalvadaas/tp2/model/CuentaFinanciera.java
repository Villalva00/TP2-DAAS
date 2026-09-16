package com.carrillovillalvadaas.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuenta_financiera")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class CuentaFinanciera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true,  length = 22)
    private long cbu;

    @Column(nullable = false, unique = true,  length = 50)
    private String alias;

    @Column(name = "saldo_operativo", nullable = false)
    private Double  saldoOperativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado ;




    public void actualizarSaldo(Double monto) {
        if (monto != null) {
            this.saldoOperativo += monto;
        }
    }

    public abstract void extraer(Double monto);

    public void depositar(Double monto) {
        if (monto != null && monto > 0) {
            this.saldoOperativo += monto;
        }
    }
}
