package com.ifsul.backend.aula.dto;

public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    Integer idade
) {
}
