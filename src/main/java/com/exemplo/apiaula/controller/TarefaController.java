package com.exemplo.apiaula.controller;

import static com.exemplo.apiaula.config.AutenticacaoInterceptor.ATRIBUTO_USUARIO_LOGADO;

import com.exemplo.apiaula.dto.TarefaRequest;
import com.exemplo.apiaula.dto.TarefaResponse;
import com.exemplo.apiaula.service.ExportacaoService;
import com.exemplo.apiaula.service.TarefaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

// Todas as rotas exigem login e trabalham apenas com as tarefas do usuário logado
@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService service;
    private final ExportacaoService exportacaoService;

    public TarefaController(TarefaService service, ExportacaoService exportacaoService) {
        this.service = service;
        this.exportacaoService = exportacaoService;
    }

    @GetMapping
    public List<TarefaResponse> listar(@RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
        return service.listar(usuarioLogadoId);
    }

    @GetMapping("/exportar/json")
    public ResponseEntity<String> exportarJson(@RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
        String json = exportacaoService.exportarTarefasEmJson(usuarioLogadoId);
        // "attachment" faz o navegador baixar o conteúdo como arquivo
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tarefas.json\"")
                .body(json);
    }

    @GetMapping("/{id}")
    public TarefaResponse buscar(@PathVariable Long id,
            @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
        return service.buscarPorId(id, usuarioLogadoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse cadastrar(@RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId,
            @RequestBody @Valid TarefaRequest request) {
        return service.cadastrar(usuarioLogadoId, request);
    }

    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable Long id,
            @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId,
            @RequestBody @Valid TarefaRequest request) {
        return service.atualizar(id, usuarioLogadoId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id,
            @RequestAttribute(ATRIBUTO_USUARIO_LOGADO) Long usuarioLogadoId) {
        service.excluir(id, usuarioLogadoId);
    }
}
