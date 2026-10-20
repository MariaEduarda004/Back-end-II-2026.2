package com.exemplo.apiaula.controller;

import static com.exemplo.apiaula.config.AutenticacaoInterceptor.ATRIBUTO_USUARIO_LOGADO;

import com.exemplo.apiaula.dto.UsuarioRequest;
import com.exemplo.apiaula.dto.UsuarioResponse;
import com.exemplo.apiaula.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // Público: é o cadastro de novos usuários
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody @Valid UsuarioRequest request) {
        return service.cadastrar(request);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id,
            @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
        return service.buscarPorId(id, usuarioLogadoId);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id,
            @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId,
            @RequestBody @Valid UsuarioRequest request) {
        return service.atualizar(id, usuarioLogadoId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id,
            @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
        service.excluir(id, usuarioLogadoId);
    }
}
