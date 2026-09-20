package com.carrillovillalvadaas.tp2.model.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter


@MappedSuperclass
//Le indica a Hibernate/JPA que esta clase no es una tabla en la base de datos por sí misma, pero que sus atributos (createdDate y lastModifiedDate) deben ser heredados y mapeados como columnas en las tablas de las entidades hijas que extiendan de esta clase.



@EntityListeners(AuditingEntityListener.class)
//Conecta la entidad con el mecanismo de escucha de Spring Data JPA. Este "oyente" detecta cuándo una entidad está a punto de ser guardada o actualizada en la base de datos para inyectar los valores correspondientes.





public abstract class EntidadAuditable {

    @CreatedDate
    //Le indica a Spring Data que debe rellenar automáticamente este campo con la fecha y hora exacta en el momento en que la entidad se crea por primera vez.


    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    //Indica que este campo debe actualizarse automáticamente con la fecha y hora actual cada vez que la entidad sufra algún cambio o actualización.


    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}