package cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaqueteDTO(
    @NotBlank(message = "La descripción del paquete es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    String descripcion,

    @NotNull(message = "El peso del paquete es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero")
    @Digits(integer = 3, fraction = 2, message = "El peso admite máximo 999.99 kg con 2 decimales")
    BigDecimal pesoKg
) {}