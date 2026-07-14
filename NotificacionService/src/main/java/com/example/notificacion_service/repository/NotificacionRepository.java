package com.example.notificacion_service.repository;

import com.example.notificacion_service.model.Notificacion;
import com.example.notificacion_service.model.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioId(Long usuarioId);
    List<Notificacion> findByTipoNotificacion(TipoNotificacion tipoNotificacion);
    List<Notificacion> findByUsuarioIdAndLeida(Long usuarioId, Boolean leida);
    List<Notificacion> findByLeida(Boolean leida);
}
