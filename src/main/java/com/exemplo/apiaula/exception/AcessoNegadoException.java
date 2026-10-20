package com.exemplo.apiaula.exception;

// Resulta em HTTP 403: o usuário está logado, mas tentou mexer em dados de outra pessoa
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Você não tem permissão para acessar este recurso");
    }
}
