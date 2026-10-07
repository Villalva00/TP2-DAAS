package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.TokenActivacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio encargado de gestionar la persistencia y las operaciones de base de datos
 * para la entidad {@link TokenActivacion}.
 *
 * Extiende de {@link JpaRepository} para proveer operaciones CRUD estándar de forma automática.
 */
@Repository
public interface TokenActivacionRepository extends JpaRepository<TokenActivacion, UUID> {

    /**
     * Busca un token de activación a partir del valor UUID que recibió el cliente por email.
     *
     * @param token Valor UUID del token.
     * @return Un {@link Optional} que contiene al token si existe, o vacío en caso contrario.
     */
    Optional<TokenActivacion> findByToken(UUID token);

    /**
     * Elimina los tokens de activación pertenecientes a un cliente.
     * Se invoca antes de eliminar al cliente para respetar la clave foránea.
     *
     * @param clienteId Identificador del cliente.
     */
    void deleteByClienteId(UUID clienteId);
}
