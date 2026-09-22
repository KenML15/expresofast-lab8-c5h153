package cr.ac.ucr.paraiso.ie.c5h153.expresofast.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO;
import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    @Query("SELECT new cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO(" +
           "e.id, e.codigoRastreo, e.direccionDestino, e.pesoKg, e.costo, e.estadoEnvio, " +
           "COALESCE(v.placa, 'N/A'), " +
           "COALESCE(CONCAT(c.nombre, ' ', c.apellidos), 'Sin Asignar')) " +
           "FROM Envio e LEFT JOIN e.vehiculo v LEFT JOIN e.conductor c")
    List<EnvioResponseDTO> findAllDtoWithDetails();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :nuevoEstado WHERE e.vehiculo.id = :vehiculoId")
    void updateEstadoEnvioByVehiculoId(@Param("vehiculoId") Integer vehiculoId, @Param("nuevoEstado") String nuevoEstado);
}