package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Repositorio encargado de gestionar la persistencia y las operaciones de base de datos
 * para la entidad {@link Cliente}.
 *
 * Extiende de {@link JpaRepository} para proveer operaciones CRUD(Guardar, Buscar, Listar, Borrar) estándar de forma automática.
 */

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    /**
     * Busca un cliente a partir de su número de CUIL único.
     *
     * @param cuil Número de CUIL del cliente.
     * @return Un {@link Optional} que contiene al cliente si se encuentra registrado, o vacío en caso contrario.
     */
    Optional<Cliente> findByCuil(String cuil);


    /**
     * Busca un cliente utilizando su dirección de correo electrónico.
     *
     * @param email Correo electrónico asociado al cliente.
     * @return Un {@link Optional} con el resultado de la búsqueda.
     */
    Optional<Cliente> findByEmail(String email);



    /**
     * Recupera una lista de clientes cuyos nombres contengan la cadena de texto proporcionada,
     * ignorando las diferencias entre mayúsculas y minúsculas.
     *
     * @param nombre Fragmento o nombre completo a buscar.
     * @return Una {@link List} de clientes que coinciden con el filtro de búsqueda.
     */
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
}

