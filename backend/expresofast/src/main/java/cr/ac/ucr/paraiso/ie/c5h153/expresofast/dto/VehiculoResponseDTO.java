package cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto;

import java.math.BigDecimal;

public class VehiculoResponseDTO {

    private Integer id;
    private String placa;
    private BigDecimal capacidadKg;
    private String estado;
    private String nombreEmpresa;

    public VehiculoResponseDTO(Integer id, String placa, BigDecimal capacidadKg, String estado, String nombreEmpresa) {
        this.id = id;
        this.placa = placa;
        this.capacidadKg = capacidadKg;
        this.estado = estado;
        this.nombreEmpresa = nombreEmpresa;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public BigDecimal getCapacidadKg() { return capacidadKg; }
    public void setCapacidadKg(BigDecimal capacidadKg) { this.capacidadKg = capacidadKg; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }
}