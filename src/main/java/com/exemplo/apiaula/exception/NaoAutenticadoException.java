package com.exemplo.apiaula.exception;

// Resulta em HTTP 401: não há login válido
public class NaoAutenticadoException extends RuntimeException {

    public NaoAutenticadoException(String mensagem) {
        super(mensagem);
    }
}
