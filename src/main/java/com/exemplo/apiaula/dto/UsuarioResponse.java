package com.exemplo.apiaula.dto;

public record UsuarioResponse(Long id, String nome, String email, Integer idade, Boolean ativo) {
}