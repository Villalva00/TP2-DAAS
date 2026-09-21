package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.EstadoTransaccion;
import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}
