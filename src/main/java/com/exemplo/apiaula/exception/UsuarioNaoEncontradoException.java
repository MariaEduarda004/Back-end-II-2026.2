package com.exemplo.apiaula.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(Long id) {
        super("Usuário com id " + id + " não encontrado");
    }
}