package cr.ac.ucr.paraiso.ie.c5h153.expresofast.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import cr.ac.ucr.paraiso.ie.c5h153.expresofast.domain.Envio;
import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo v JOIN FETCH v.empresa JOIN FETCH e.conductor")
    List<Envio> findAllWithDetails();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :nuevoEstado WHERE e.vehiculo.id = :vehiculoId")
    void updateEstadoEnvioByVehiculoId(@Param("vehiculoId") Integer vehiculoId, @Param("nuevoEstado") String nuevoEstado);
}
