package cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EnvioConPaquetesDTO(
    Integer id,
    String codigoRastreo,
    String destinatario,
    String direccionDestino,
    BigDecimal montoFlete,
    String estado,
    LocalDate fechaDespacho,
    LocalDate fechaEntregaEstimada,
    BigDecimal pesoTotalKg,
    List<PaqueteDTO> paquetes
) {}