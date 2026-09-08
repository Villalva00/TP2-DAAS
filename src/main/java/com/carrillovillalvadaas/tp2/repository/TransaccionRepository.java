package com.carrillovillalvadaas.tp2.repository;

import com.carrillovillalvadaas.tp2.model.TipoTransaccion;
import com.carrillovillalvadaas.tp2.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByCuentaId(Long cuentaId);
    List<Transaccion> findByTipo(TipoTransaccion tipo);
}