package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Query Method 1: Spring Data genera automáticamente "SELECT * FROM cliente WHERE cuil = ?"
    Optional<Cliente> findByCuil(String cuil);

    // Query Method 2: Spring Data genera automáticamente "SELECT * FROM cliente WHERE email = ?"
    Optional<Cliente> findByEmail(String email);
}

