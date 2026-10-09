package com.carrillovillalvadaas.tp2.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica la presencia del tipo de transacción {@code DEBITO_COMISION} usado por
 * la liquidación mensual de comisiones.
 */
class TipoTransaccionTest {

    @Test
    void deberiaExistirElTipoDebitoComision() {
        assertThat(TipoTransaccion.valueOf("DEBITO_COMISION")).isEqualTo(TipoTransaccion.DEBITO_COMISION);
        assertThat(TipoTransaccion.values()).contains(TipoTransaccion.DEBITO_COMISION);
    }
}