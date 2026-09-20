package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.EntidadAuditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;




import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "cliente ")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente extends EntidadAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "razon_social", length = 100)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private long cuil;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 200)
    private String direccion;

    @Column(length = 30)
    private long telefono;

    // Relación reflexiva validada en tu diagrama UML (1 a muchos entre clientes)
    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "cliente_padre_id")
    //private Cliente clientePrincipal;




    //public Cliente getClientePrincipal() { return clientePrincipal; }
    //public void setClientePrincipal(Cliente clientePrincipal) { this.clientePrincipal = clientePrincipal; }
}