package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.EntidadAuditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa un cliente del sistema financiero, el cual puede poseer cuentas financieras y mantener relaciones jerárquicas con otros clientes.
 * Extiende de {@link EntidadAuditable}.
 *
 * @see EntidadAuditable
 * @see CuentaFinanciera
 */


/**
 * Le indica a JPA que esta clase es una entidad persistente y que estará mapeada a una tabla en la base de datos.
 */
@Entity

/**
 * //Define explícitamente el nombre que tendrá la tabla en la base de datos (cliente).
*/
@Table(name = "cliente ")


@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Cliente extends EntidadAuditable {


    /**
     * Identificador unico autogenerado del cliente.
     */
    @Id
    @GeneratedValue
    private UUID id;


    /**
     * Nombre y apellido completo del cliente.
     */
    @Column(name="nombre", nullable = false, length = 100)
    private String nombre;



    /**
     * Razón social del cliente, aplicable en caso de personas jurídicas.
     */
    @Column(name = "razon_social", length = 100)
    private String razonSocial;


    /**
     * Numero de cuil del cliente.
     */
    @Column(nullable = false, unique = true, length = 11)
    private long cuil;


    /**
     * Correo electronico de contacto del cliente.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;



    /**
     * Dirección física de residencia o domicilio del cliente.
     */
    @Column(length = 200)
    private String direccion;

    /**
     * Número de teléfono de contacto del cliente.
     */
    @Column(length = 30)
    private String telefono;

    /**
     *  Relación reflexiva de 1 a muchos entre clientes
     * @ManyToOne Define que muchos clientes secundarios pueden estar asociados a un único clientePrincipal
     *
     *fetch = FetchType.LAZY Cuando traes un cliente secundario de la base de datos, no trae los datos del cliente principal de inmediato, sino solo cuando lo solicitas explícitamente optimizando las consultas SQL.
     *
     * @JoinColumn(name = "cliente_padre_id") Crea físicamente una columna en la tabla cliente de tu base de datos llamada cliente_padre_id. Esa columna funcionará como una llave foránea ( que guardará el ID de otro registro de la misma tabla cliente. Si un cliente es titular independiente, esa columna estará vacía (null); si es un cliente adherido, contendrá el ID del cliente principal.
     *
     * private Cliente clientePrincipal es el atributo en Java que guardará el objeto del cliente principal al que está vinculado este sub-cliente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_padre_id")
    private Cliente clientePrincipal;


    /**
     *  Relacion de uno a muchos con CuentaFinanciera
     *
     *  mappedBy = "cliente": Señala que el campo cliente en la clase CuentaFinanciera es el dueño de la relación en la base de datos
     *
     *  cascade = CascadeType.ALL: Significa que cualquier operación que hagas en el cliente (guardarlo, borrarlo, etc.) se propagará automáticamente a sus cuentas.
     *
     *  orphanRemoval = true: Si remueves una cuenta de la lista del cliente, Hibernate la elimina automáticamente de la base de datos por considerarla un "huérfano"
     *
     *  @Builder.Default y = new ArrayList<>(), sirve para asegurar que la lista de cuentas siempre esté creada y vacía en lugar de ser nula, evitando errores de tipo NullPointerException cuando se intenta agregarle una cuenta por primera vez.
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch =   FetchType.LAZY)
    @Builder.Default
    private List<CuentaFinanciera> cuentas = new ArrayList<>();


}