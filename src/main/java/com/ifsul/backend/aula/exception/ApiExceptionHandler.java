package com.ifsul.backend.aula.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(RecursoNaoEncontradoException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Map.of("erro", exception.getMessage()));
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, String>> conflito(EmailDuplicadoException exception) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(Map.of("erro", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalido(MethodArgumentNotValidException exception) {
        String mensagem = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(erro -> erro.getDefaultMessage())
            .orElse("Dados inválidos");

        return ResponseEntity
            .badRequest()
            .body(Map.of("erro", mensagem));
    }
}
