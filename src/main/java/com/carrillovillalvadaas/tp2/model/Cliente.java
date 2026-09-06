package com.carrillovillalvadaas.tp2.model;

import com.carrillovillalvadaas.tp2.model.audit.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cliente")
public class Cliente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 11)
    private String cuil;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(length = 200)
    private String direccion;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<CuentaFinanciera> cuentas = new ArrayList<>();

    public Cliente(String nombre, String cuil, String email) {
        this.nombre = nombre;
        this.cuil = cuil;
        this.email = email;
    }

    public void agregarCuenta(CuentaFinanciera cuenta) {
        if (cuenta.getCliente() != null && !cuenta.getCliente().equals(this)) {
            throw new IllegalStateException("La cuenta ya pertenece a otro cliente");
        }
        cuenta.setCliente(this);
        this.cuentas.add(cuenta);
    }
}