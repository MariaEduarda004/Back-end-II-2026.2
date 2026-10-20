package com.exemplo.apiaula.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,
        @NotBlank(message = "email é obrigatório")
        @Email(message = "email inválido")
        String email,
        @NotNull(message = "idade é obrigatória")
        @Min(value = 0, message = "idade mínima é 0")
        Integer idade,
        @NotNull(message = "ativo é obrigatório")
        Boolean ativo,
        @NotBlank(message = "senha é obrigatória")
        @Size(min = 6, max = 72, message = "senha deve ter entre 6 e 72 caracteres")
        String senha) {
}
