package com.exemplo.apiaula.service;

import com.exemplo.apiaula.dto.UsuarioRequest;
import com.exemplo.apiaula.dto.UsuarioResponse;
import com.exemplo.apiaula.entity.Usuario;
import com.exemplo.apiaula.exception.AcessoNegadoException;
import com.exemplo.apiaula.exception.EmailDuplicadoException;
import com.exemplo.apiaula.exception.UsuarioComTarefasException;
import com.exemplo.apiaula.exception.UsuarioNaoEncontradoException;
import com.exemplo.apiaula.repository.TarefaRepository;
import com.exemplo.apiaula.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final TarefaRepository tarefaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, TarefaRepository tarefaRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.tarefaRepository = tarefaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponse cadastrar(UsuarioRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        Usuario usuario = new Usuario(null, request.nome(), request.email(), request.idade(),
                request.ativo(), passwordEncoder.encode(request.senha()));
        return toResponse(repository.save(usuario));
    }

    public UsuarioResponse buscarPorId(Long id, Long usuarioLogadoId) {
        return toResponse(buscarDoUsuarioLogado(id, usuarioLogadoId));
    }

    public UsuarioResponse atualizar(Long id, Long usuarioLogadoId, UsuarioRequest request) {
        Usuario usuario = buscarDoUsuarioLogado(id, usuarioLogadoId);

        boolean trocouEmail = !usuario.getEmail().equalsIgnoreCase(request.email());
        if (trocouEmail && repository.existsByEmail(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setIdade(request.idade());
        usuario.setAtivo(request.ativo());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        return toResponse(repository.save(usuario));
    }

    public void excluir(Long id, Long usuarioLogadoId) {
        buscarDoUsuarioLogado(id, usuarioLogadoId);

        if (tarefaRepository.existsByUsuarioId(id)) {
            throw new UsuarioComTarefasException(id);
        }
        repository.deleteById(id);
    }

    // Cada usuário só pode ver e alterar a si mesmo
    private Usuario buscarDoUsuarioLogado(Long id, Long usuarioLogadoId) {
        if (!id.equals(usuarioLogadoId)) {
            throw new AcessoNegadoException();
        }
        return repository.findById(id).orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getIdade(), usuario.getAtivo());
    }
}
