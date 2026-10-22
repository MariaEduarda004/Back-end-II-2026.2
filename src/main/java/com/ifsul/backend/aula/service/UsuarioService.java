package com.ifsul.backend.aula.service;

import com.ifsul.backend.aula.dto.UsuarioRequest;
import com.ifsul.backend.aula.dto.UsuarioResponse;
import com.ifsul.backend.aula.entity.Usuario;
import com.ifsul.backend.aula.exception.EmailDuplicadoException;
import com.ifsul.backend.aula.exception.RecursoNaoEncontradoException;
import com.ifsul.backend.aula.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {

        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll()
            .stream()
            .map(this::converter)
            .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        Usuario usuario = localizar(id);
        return converter(usuario);
    }

    public UsuarioResponse cadastrar(UsuarioRequest dados) {
        if (repository.existsByEmailIgnoreCase(dados.email())) {
            throw new EmailDuplicadoException(dados.email());
        }

        Usuario novoUsuario = new Usuario(
            null,
            dados.nome(),
            dados.email(),
            dados.idade()
        );

        Usuario usuarioSalvo = repository.save(novoUsuario);
        return converter(usuarioSalvo);
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest dados) {
        Usuario usuario = localizar(id);

        boolean emailFoiAlterado = !usuario.getEmail().equalsIgnoreCase(dados.email());
        boolean emailJaEstaCadastrado = emailFoiAlterado
            && repository.existsByEmailIgnoreCase(dados.email());

        if (emailFoiAlterado && emailJaEstaCadastrado) {
            throw new EmailDuplicadoException(dados.email());
        }

        usuario.setNome(dados.nome());
        usuario.setEmail(dados.email());
        usuario.setIdade(dados.idade());

        Usuario usuarioAtualizado = repository.save(usuario);
        return converter(usuarioAtualizado);
    }

    public void excluir(Long id) {
        Usuario usuario = localizar(id);
        repository.delete(usuario);
    }

    private Usuario localizar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    private UsuarioResponse converter(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getIdade()
        );
    }
}
