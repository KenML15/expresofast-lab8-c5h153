package cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnvioDTO(
    Integer id,
    String codigoRastreo,
    String destinatario,
    String direccionDestino,
    BigDecimal montoFlete,
    String estado,
    LocalDateTime fechaCreacion
) {}