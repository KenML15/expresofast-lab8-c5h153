package cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EnvioRegistroDTO(
    @NotBlank(message = "El número de tracking es obligatorio")
    @Size(max = 30, message = "El número de tracking no puede superar 30 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "El tracking solo admite letras, números y guiones")
    String numeroTracking,

    @NotBlank(message = "El destinatario es obligatorio")
    @Size(max = 120, message = "El destinatario no puede superar 120 caracteres")
    String destinatario,

    @NotBlank(message = "La dirección de destino es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    String direccionDestino,

    @NotNull(message = "El monto de flete es obligatorio")
    @Positive(message = "El monto de flete debe ser mayor a cero")
    BigDecimal montoFlete,

    @NotNull(message = "La fecha de despacho es obligatoria")
    LocalDate fechaDespacho,

    @NotNull(message = "La fecha de entrega estimada es obligatoria")
    LocalDate fechaEntregaEstimada,

    @NotEmpty(message = "El envío debe contener al menos 1 paquete")
    List<@Valid PaqueteDTO> paquetes
) {
    @JsonIgnore
    @AssertTrue(message = "La fecha de entrega estimada debe ser posterior a la fecha de despacho")
    public boolean isRangoFechasValido() {
        if (fechaDespacho == null || fechaEntregaEstimada == null) {
            return true; 
        }
        return fechaEntregaEstimada.isAfter(fechaDespacho);
    }
}