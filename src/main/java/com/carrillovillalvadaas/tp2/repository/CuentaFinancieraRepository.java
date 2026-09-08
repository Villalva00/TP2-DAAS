package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaFinancieraRepository extends JpaRepository<CuentaFinanciera, Long> {
    Optional<CuentaFinanciera> findByCbu(String cbu);
    Optional<CuentaFinanciera> findByAlias(String alias);
    List<CuentaFinanciera> findByClienteId(Long clienteId);
    List<CuentaFinanciera> findByEstado(EstadoCuenta estado);
}