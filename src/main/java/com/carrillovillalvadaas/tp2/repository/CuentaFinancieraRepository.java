package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.CuentaFinanciera;
import com.carrillovillalvadaas.tp2.model.EstadoCuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CuentaFinancieraRepository extends JpaRepository<CuentaFinanciera, Long> {
    // Query Method 1: Busca una cuenta por su CBU único
    Optional<CuentaFinanciera> findByCbu(String cbu);

    // Query Method 2: Retorna la lista de todas las cuentas que tengan cierto estado (ej: ACTIVA)
    List<CuentaFinanciera> findByEstado(EstadoCuenta estado);



}
