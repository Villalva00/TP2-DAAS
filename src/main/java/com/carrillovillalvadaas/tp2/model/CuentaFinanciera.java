package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.EntidadAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


/**
 * Superclase abstracta que representa una cuenta financiera dentro del sistema bancario.
 * Posee relaciones de pertenencia con {@link Cliente} y de composición con {@link Transaccion}.
 * Utiliza la estrategia de herencia {@link InheritanceType#JOINED}.
 * Posee relaciones de pertenencia con {@link  Cliente} y de composicion (1:N) con {@link Transaccion}
 * @see EntidadAuditable
 * @see Cliente
 * Transaccion
 */



/**
 * Le indica a JPA que esta clase es una entidad persistente y que estará mapeada a una tabla en la base de datos.
 */
@Entity


/**
 * //Define explícitamente el nombre que tendrá la tabla en la base de datos (cuenta_financiera).
 */
@Table(name = "cuenta_financiera")


/**
 * Es la estrategia que le indica a Hibernate/JPA cómo traducir una jerarquía de clases de Java (donde unas heredan de otras) a tablas relacionales en una base de datos SQL.
 * La palabra clave aquí es JOINED (Unidas). Significa que cada clase en la jerarquía tendrá su propia tabla independiente, y se conectarán entre sí mediante un JOIN (una relación de clave primaria/foránea).
 */
@Inheritance(strategy = InheritanceType.JOINED)


@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor



public abstract class CuentaFinanciera extends EntidadAuditable {

    /**
     * Indica que este campo es la clave primaria (Primary Key) de la tabla, es decir, el identificador único de cada cuenta.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Clave Bancaria Uniforme (CBU) única de la cuenta.
     */
    @Column(nullable = false, unique = true,  length = 22)
    private long cbu;

    /**
     * Alias alfanumérico único asociado a la cuenta.
     */
    @Column(nullable = false, unique = true,  length = 50)
    private String alias;

    /**
     * Saldo operativo actual disponible en la cuenta.
     */
    @Column(name = "saldo_operativo", nullable = false)
    private Double  saldoOperativo;

    /**
     * Esta anotación es fundamental cuando usas un Enum (como tu clase EstadoCuenta). Le indica a Hibernate que guarde el valor en la base de datos como texto plano (por ejemplo: "ACTIVA", "SUSPENDIDA", "CERRADA") en lugar de guardarlo como un número ordinal (0, 1, 2). Guardarlo como texto hace que tu base de datos sea mucho más fácil de leer y auditar directamente.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado ;


    /**
     * Actualiza el saldo operativo sumando el monto indicado.
     *
     * @param monto Valor monetario a sumar.
     */
    public void actualizarSaldo(Double monto) {
        if (monto != null) {
            this.saldoOperativo += monto;
        }
    }

    /**
     * Realiza un depósito incrementando el saldo operativo.
     *
     * @param monto Monto monetario a depositar.
     */
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
