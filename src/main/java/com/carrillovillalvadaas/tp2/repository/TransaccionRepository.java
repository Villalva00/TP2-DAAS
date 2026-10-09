package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;




/**
 * Repositorio encargado de gestionar la persistencia y las operaciones de base de datos
 * para la entidad {@link Transaccion}.
 *
 * Extiende de {@link JpaRepository} para proveer operaciones CRUD(Guardar, Buscar, Listar, Borrar) estándar de forma automática.
 */



@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {




    /**
     * Recupera una lista con todas las transacciones asociadas a una cuenta financiera específica.
     * Utiliza el identificador de la cuenta relacionada.
     *
     * @param cuentaFinancieraId Identificador único de la cuenta financiera.
     * @return Una {@link List} de transacciones correspondientes a dicha cuenta.
     */
    List<Transaccion> findByCuentaFinancieraId(UUID cuentaFinancieraId);



    /**
     * Filtra y obtiene las transacciones según su tipo (ej. DEPOSITO, EXTRACCION, TRANSFERENCIA).
     *
     * @param tipo Categoría o tipo de transacción a consultar.
     * @return Una {@link List} de transacciones que coinciden con el tipo indicado.
     */
    List<Transaccion> findByTipo(TipoTransaccion tipo);


    /**
     * Consulta las transacciones filtrando por su estado actual (ej. COMPLETADA, PENDIENTE, RECHAZADA).
     *
     * @param estadoTransaccion Estado de la transacción a buscar.
     * @return Una {@link List} de transacciones que poseen dicho estado.
     */
    List<Transaccion> findByEstadoTransaccion(EstadoTransaccion estadoTransaccion);


    /**
     * Suma los montos de las transacciones de un ejecutor dentro de un rango de fechas,
     * filtrando por tipo y estado. Devuelve 0 cuando no hay movimientos en el período,
     * gracias al uso de {@code COALESCE}.
     *
     * @param ejecutorId Identificador único del cliente que ejecutó las operaciones (titular o adherente).
     * @param tipo       Categoría de transacción a considerar (ej. EXTRACCION).
     * @param estado     Estado de la transacción a considerar (ej. COMPLETADA).
     * @param desde      Fecha y hora inclusive de inicio del rango.
     * @param hasta      Fecha y hora exclusiva de fin del rango.
     * @return La suma de los montos o 0 (via COALESCE) si no existen registros.
     */
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t " +
            "WHERE t.ejecutor.id = :ejecutorId " +
            "AND t.tipo = :tipo " +
            "AND t.estadoTransaccion = :estado " +
            "AND t.fechaHora >= :desde " +
            "AND t.fechaHora < :hasta")
    Double sumarMontoPorEjecutorEntre(@Param("ejecutorId") UUID ejecutorId,
                                      @Param("tipo") TipoTransaccion tipo,
                                      @Param("estado") EstadoTransaccion estado,
                                      @Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta);

    /**
     * Verifica si una cuenta ya fue debitada por comisión de mantenimiento dentro
     * del período indicado. Se usa como control de idempotencia de la liquidación
     * mensual: no se vuelve a debitar una cuenta que ya pagó su comisión del mes.
     *
     * @param cuentaId Identificador de la cuenta financiera.
     * @param tipo     Tipo de transacción esperado (DEBITO_COMISION).
     * @param desde    Límite inferior inclusive del período.
     * @param hasta    Límite superior exclusivo del período.
     * @return {@code true} si ya existe una comisión debitada en el período.
     */
    @Query("SELECT COUNT(t) > 0 FROM Transaccion t " +
            "WHERE t.cuentaFinanciera.id = :cuentaId " +
            "AND t.tipo = :tipo " +
            "AND t.fechaHora >= :desde " +
            "AND t.fechaHora < :hasta")
    boolean existsDebitoComisionEnPeriodo(@Param("cuentaId") UUID cuentaId,
                                          @Param("tipo") TipoTransaccion tipo,
                                          @Param("desde") LocalDateTime desde,
                                          @Param("hasta") LocalDateTime hasta);

}
