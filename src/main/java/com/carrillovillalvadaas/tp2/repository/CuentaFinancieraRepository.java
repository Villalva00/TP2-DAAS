package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Repositorio encargado de gestionar la persistencia y las operaciones de base de datos
 * para la jerarquía de entidades {@link CuentaFinanciera}.
 *
 * Extiende de {@link JpaRepository} para proveer operaciones CRUD(Guardar, Buscar, Listar, Borrar) estándar de forma automática.
 */


public interface CuentaFinancieraRepository extends JpaRepository<CuentaFinanciera, UUID> {



    /**
     * Busca una cuenta financiera a partir de su número de CBU único.
     *
     * @param cbu Número de CBU de la cuenta.
     * @return Un {@link Optional} que contiene la cuenta si es encontrada, o vacío en caso contrario.
     */
    Optional<CuentaFinanciera> findByCbu(long cbu);



    /**
     * Busca una cuenta financiera utilizando su alias asociado.
     *
     * @param alias Alias único de la cuenta.
     * @return Un {@link Optional} con el resultado de la búsqueda.
     */
    Optional<CuentaFinanciera> findByAlias(String alias);


    /**
     * Recupera una lista con todas las cuentas financieras que pertenecen a un cliente específico.
     * Aprovecha la relación de la entidad mediante el identificador del cliente.
     *
     * @param clienteId Identificador único del cliente titular.
     * @return Una {@link List} de cuentas asociadas a dicho cliente.
     */
    List<CuentaFinanciera> findByClienteId(UUID clienteId);


    /**
     * Filtra y obtiene las cuentas financieras según su estado actual (ej. ACTIVA, SUSPENDIDA, BLOQUEADA).
     *
     * @param estado Estado de la cuenta a consultar.
     * @return Una {@link List} de cuentas que coinciden con el estado indicado.
     */
    List<CuentaFinanciera> findByEstado(EstadoCuenta estado);



}
