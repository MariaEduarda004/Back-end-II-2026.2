package com.exemplo.apiaula.repository;

import com.exemplo.apiaula.entity.Tarefa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByUsuarioId(Long usuarioId);

    Optional<Tarefa> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
