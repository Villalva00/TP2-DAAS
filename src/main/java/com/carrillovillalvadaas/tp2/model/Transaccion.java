package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.EntidadAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa una transacción monetaria o movimiento registrado dentro de una cuenta financiera.
 * Extiende de {@link EntidadAuditable} para registrar automáticamente las fechas de creación y modificación.
 *
 * @see CuentaFinanciera
 * @see EstadoTransaccion
 * @see TipoTransaccion
 */




@Entity
@Table(name = "transaccion")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaccion extends EntidadAuditable {

    /**
     * Identificador único de la transacción, generado de forma autoincremental por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Fecha y hora exacta en la que se llevó a cabo la transacción.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Monto monetario involucrado en la transacción.
     */
    @Column(nullable = false)
    private Double monto;

    /**
     * Estado actual en el que se encuentra la transacción (ej. COMPLETADA, PENDIENTE, RECHAZADA).
     * Se almacena como texto plano en la base de datos gracias a {@link Enumerated}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTransaccion estadoTransaccion;

    /**
     * Categoría o tipo de la transacción (ej. DEPOSITO, EXTRACCION, TRANSFERENCIA).
     * Almacenado como cadena de texto para mayor legibilidad en la base de datos.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTransaccion tipo;

    /**
     * Cuenta financiera a la cual pertenece esta transacción.
     *
     * @ManyToOne Define una cardinalidad de muchos a uno: muchas transacciones
     * pueden estar asociadas a una única {@link CuentaFinanciera}.
     *
     * fetch = FetchType.LAZY Carga los datos de la cuenta de forma diferida, optimizando
     * el rendimiento de las consultas al no traer la cuenta completa a menos que se solicite.
     *
     * @JoinColumn(name = "cuenta_financiera_id", nullable = false) Crea físicamente
     * la clave foránea en la tabla transaccion. El parámetro nullable = false asegura
     * que toda transacción esté obligatoriamente asociada a una cuenta.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_financiera_id", nullable = false)
    private CuentaFinanciera cuentaFinanciera;
}