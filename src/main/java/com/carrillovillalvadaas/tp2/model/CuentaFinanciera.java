package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.EntidadAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


/**
 * Posee relaciones de pertenencia con {@link  Cliente} y de composicion (1:N) con {@link Transaccion}
 * @see EntidadAuditable
 * @see Cliente
 * Transaccion
 */




@Entity
//Le indica a JPA que esta clase es una entidad persistente y que estará mapeada a una tabla en la base de datos.


@Table(name = "cuenta_financiera")
//Define explícitamente el nombre que tendrá la tabla en la base de datos (cuenta_financiera).


@Inheritance(strategy = InheritanceType.JOINED)
//es la estrategia que le indica a Hibernate/JPA cómo traducir una jerarquía de clases de Java (donde unas heredan de otras) a tablas relacionales en una base de datos SQL.
//La palabra clave aquí es JOINED (Unidas). Significa que cada clase en la jerarquía tendrá su propia tabla independiente, y se conectarán entre sí mediante un JOIN (una relación de clave primaria/foránea).


@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor



public abstract class CuentaFinanciera extends EntidadAuditable {

    @Id
    //Indica que este campo es la clave primaria (Primary Key) de la tabla, es decir, el identificador único de cada cuenta.


    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true,  length = 22)
    private long cbu;

    @Column(nullable = false, unique = true,  length = 50)
    private String alias;

    @Column(name = "saldo_operativo", nullable = false)
    private Double  saldoOperativo;

    @Enumerated(EnumType.STRING)
    //Esta anotación es fundamental cuando usas un Enum (como tu clase EstadoCuenta). Le indica a Hibernate que guarde el valor en la base de datos como texto plano (por ejemplo: "ACTIVA", "SUSPENDIDA", "CERRADA") en lugar de guardarlo como un número ordinal (0, 1, 2). Guardarlo como texto hace que tu base de datos sea mucho más fácil de leer y auditar directamente.

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






    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
