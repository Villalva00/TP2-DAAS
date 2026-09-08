package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByEmail(String email);
    boolean existsByDni(String dni);
}