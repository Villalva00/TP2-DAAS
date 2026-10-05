package com.carrillovillalvadaas.tp2.dto;
import com.carrillovillalvadaas.tp2.model.Parentesco;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.context.annotation.PropertySource;

/**
 * DTO para el alta de un adherente (cónyuge o hijo) asociado a un titular.
 * Reutiliza las validaciones de {@link ClienteRequestDto} y agrega el parentesco.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdherenteRequestDto {
    @NotBlank(message="El nombre es obligatorio.")
    @Size(max=100,message="El nombre no puede superar los 100 caracteres.")
    private String nombre;

    @NotBlank(message = "El CUIL es obligatorio.")
    @Pattern(regexp = "\\d{11}", message = "El CUIL debe contener exactamente 11 digitos numericos sin guion.")
    private String cuil;

    /**
     * Correo electrónico de contacto del cliente.
     * Debe ser único y cumplir con una estructura de email válida.
     */
    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "Debe proporcionar una dirección de correo electrónico válida.")
    private String email;

    /**
     * Número de teléfono de contacto del cliente.
     */
    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 15, message = "El numero de telefono no debe superar los 15 digitos numericos.")
    private String telefono;

    /**
     * Dirección física o domicilio postal del cliente.
     */
    @NotBlank(message = "La dirección es obligatoria.")
    private String direccion;
    /*
    *Parentesco con el titular(CONYUGE HIJO)
     */
    @NotNull(message="El parentesco es obligatorio.")
    private Parentesco parentesco;
}
