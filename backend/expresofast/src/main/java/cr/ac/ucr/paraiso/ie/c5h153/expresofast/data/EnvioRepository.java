package cr.ac.ucr.paraiso.ie.c5h153.expresofast.data;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.dto.EnvioResponseDTO;

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
    void updateEstadoEnvioByVehiculoId(@Param("vehiculoId") Integer vehiculoId,
            @Param("nuevoEstado") String nuevoEstado);

    @Procedure(procedureName = "SP_OBTENER_ENVIOS_POR_ESTADO")
    List<Envio> llamarSpEnviosPorEstado(@Param("pEstado") String pEstado);

    @EntityGraph(attributePaths = { "vehiculo", "conductor" })
    Page<Envio> findByEstadoEnvio(String estadoEnvio, Pageable pageable);

    @EntityGraph(attributePaths = { "vehiculo", "conductor" })
    @Query("SELECT e FROM Envio e")
    Page<Envio> findAllPaginado(Pageable pageable);

    @EntityGraph(attributePaths = { "vehiculo", "conductor" })
    @Query("SELECT e FROM Envio e WHERE LOWER(e.codigoRastreo) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "OR LOWER(e.direccionDestino) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Envio> buscarPorTerminoPaginado(@Param("busqueda") String busqueda, Pageable pageable);
}
