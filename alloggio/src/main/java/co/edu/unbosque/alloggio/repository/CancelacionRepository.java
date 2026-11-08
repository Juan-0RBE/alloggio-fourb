package co.edu.unbosque.alloggio.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.alloggio.entity.Cancelacion;

public interface CancelacionRepository extends JpaRepository<Cancelacion, Long> {
}