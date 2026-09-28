package com.carrillovillalvadaas.tp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Objeto de transferencia de datos (DTO) para la recepción y validación
 * de la información de un Cliente al momento de su registro o actualización.
 * Actúa como la frontera de entrada en la capa Controller.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDto {

    /**
     * Nombre completo o razón social del cliente.
     * Es un campo obligatorio y no puede superar los 100 caracteres.
     */
    @NotBlank(message = "El nombre o razón social es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombre;

    /**
     * Clave Única de Identificación Laboral/Tributaria (CUIL) del cliente.
     * Debe respetar estrictamente el formato estándar XX-XXXXXXXX-X.
     */
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
}