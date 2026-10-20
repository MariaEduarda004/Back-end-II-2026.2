package com.exemplo.apiaula.service;

import com.exemplo.apiaula.dto.TarefaRequest;
import com.exemplo.apiaula.dto.TarefaResponse;
import com.exemplo.apiaula.entity.Tarefa;
import com.exemplo.apiaula.entity.Usuario;
import com.exemplo.apiaula.exception.TarefaNaoEncontradaException;
import com.exemplo.apiaula.exception.UsuarioNaoEncontradoException;
import com.exemplo.apiaula.repository.TarefaRepository;
import com.exemplo.apiaula.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository repository;
    private final UsuarioRepository usuarioRepository;

    public TarefaService(TarefaRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listar(Long usuarioLogadoId) {
        return repository.findByUsuarioId(usuarioLogadoId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id, Long usuarioLogadoId) {
        return toResponse(buscarTarefaDoUsuario(id, usuarioLogadoId));
    }

    public TarefaResponse cadastrar(Long usuarioLogadoId, TarefaRequest request) {
        Usuario dono = usuarioRepository.findById(usuarioLogadoId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioLogadoId));

        Tarefa tarefa = new Tarefa(null, request.titulo(), request.concluida(), dono);
        return toResponse(repository.save(tarefa));
    }

    public TarefaResponse atualizar(Long id, Long usuarioLogadoId, TarefaRequest request) {
        Tarefa tarefa = buscarTarefaDoUsuario(id, usuarioLogadoId);
        tarefa.setTitulo(request.titulo());
        tarefa.setConcluida(request.concluida());
        return toResponse(repository.save(tarefa));
    }

    public void excluir(Long id, Long usuarioLogadoId) {
        repository.delete(buscarTarefaDoUsuario(id, usuarioLogadoId));
    }

    // Tarefa de outro usuário responde "não encontrada", sem revelar que ela existe
    private Tarefa buscarTarefaDoUsuario(Long id, Long usuarioLogadoId) {
        return repository.findByIdAndUsuarioId(id, usuarioLogadoId)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    private TarefaResponse toResponse(Tarefa tarefa) {
        return new TarefaResponse(tarefa.getId(), tarefa.getTitulo(), tarefa.getConcluida());
    }
}
