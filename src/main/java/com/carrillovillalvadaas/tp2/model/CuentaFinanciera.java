package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cuenta", discriminatorType = DiscriminatorType.STRING, length = 30)
public abstract class CuentaFinanciera extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 22)
    private String cbu;

    @Column(unique = true, nullable = false, length = 30)
    private String alias;

    @Column(name = "saldo_operativo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoOperativo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado = EstadoCuenta.ACTIVA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "cuenta", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Transaccion> transacciones = new ArrayList<>();

    protected CuentaFinanciera(String cbu, String alias, Cliente cliente) {
        this.cbu = cbu;
        this.alias = alias;
        this.cliente = cliente;
    }

    public void depositar(BigDecimal monto, String detalle) {
        validarMonto(monto);
        this.saldoOperativo = this.saldoOperativo.add(monto);
        registrarTransaccion(TipoTransaccion.DEPOSITO, monto, detalle);
    }

    public boolean extraer(BigDecimal monto, String detalle) {
        validarMonto(monto);
        if (this.saldoOperativo.compareTo(monto) < 0) {
            return false;
        }
        this.saldoOperativo = this.saldoOperativo.subtract(monto);
        registrarTransaccion(TipoTransaccion.EXTRACCION, monto, detalle);
        return true;
    }

    public void transferir(CuentaFinanciera destino, BigDecimal monto, String detalle) {
        if (destino == null || this.equals(destino)) {
            throw new IllegalArgumentException("La cuenta destino no puede ser nula ni igual a la cuenta origen");
        }
        if (!extraer(monto, detalle)) {
            throw new IllegalStateException("Saldo insuficiente para transferir " + monto);
        }
        destino.depositar(monto, detalle);
    }

    public BigDecimal verificarSaldo() {
        return this.saldoOperativo;
    }

    protected void registrarTransaccion(TipoTransaccion tipo, BigDecimal monto, String detalle) {
        Transaccion transaccion = new Transaccion(tipo, monto, detalle, this);
        this.transacciones.add(transaccion);
    }

    protected void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }
}