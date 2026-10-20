package com.exemplo.apiaula.exception;

public class UsuarioComTarefasException extends RuntimeException {

    public UsuarioComTarefasException(Long id) {
        super("Usuário com id " + id + " possui tarefas; exclua as tarefas antes");
    }
}
