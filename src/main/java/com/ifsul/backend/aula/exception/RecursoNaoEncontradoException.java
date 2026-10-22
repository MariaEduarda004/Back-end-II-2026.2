package com.ifsul.backend.aula.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(Long id) {

        super("Usuário " + id + " não encontrado");
    }
}
