package cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearEnvioDTO(
    @NotBlank(message = "El destinatario es obligatorio")
    @Size(max = 120, message = "El destinatario no puede superar 120 caracteres")
    String destinatario,

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    String direccionDestino,

    @NotNull(message = "El monto de flete es obligatorio")
    @Positive(message = "El monto de flete debe ser mayor a cero")
    BigDecimal montoFlete
) {}
