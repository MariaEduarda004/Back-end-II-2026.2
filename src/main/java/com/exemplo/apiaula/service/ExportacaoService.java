package com.exemplo.apiaula.service;

import com.exemplo.apiaula.dto.TarefaResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Service;

// Converte as tarefas do usuário em texto JSON usando o ObjectMapper do Jackson
@Service
public class ExportacaoService {

    private final TarefaService tarefaService;
    private final ObjectMapper objectMapper;

    public ExportacaoService(TarefaService tarefaService, ObjectMapper objectMapper) {
        this.tarefaService = tarefaService;
        this.objectMapper = objectMapper;
    }

    public String exportarTarefasEmJson(Long usuarioLogadoId) {
        List<TarefaResponse> tarefas = tarefaService.listar(usuarioLogadoId);
        try {
            // Objeto Java (lista) -> String JSON, com quebras de linha para facilitar a leitura
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tarefas);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível gerar o JSON das tarefas.", e);
        }
    }
}
