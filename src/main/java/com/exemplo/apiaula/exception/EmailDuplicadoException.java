package com.exemplo.apiaula.exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException(String email) {
        super("E-mail " + email + " já cadastrado");
    }
}