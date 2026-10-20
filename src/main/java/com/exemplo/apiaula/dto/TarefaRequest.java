package com.exemplo.apiaula.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// O dono da tarefa não vem no corpo: é o usuário logado (identificado pelo token)
public record TarefaRequest(
        @NotBlank(message = "título é obrigatório")
        @Size(max = 150, message = "título deve ter no máximo 150 caracteres")
        String titulo,
        @NotNull(message = "concluida é obrigatório")
        Boolean concluida) {
}
