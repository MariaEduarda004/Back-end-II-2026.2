package com.exemplo.apiaula.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UsuarioNaoEncontradoException.class, TarefaNaoEncontradaException.class})
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RuntimeException exception) {
        return erro(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({EmailDuplicadoException.class, UsuarioComTarefasException.class})
    public ResponseEntity<ErroResposta> tratarConflito(RuntimeException exception) {
        return erro(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException exception) {
        return erro(HttpStatus.CONFLICT, "Conflito com uma restrição dos dados");
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErroResposta> tratarNaoAutenticado(NaoAutenticadoException exception) {
        return erro(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResposta> tratarAcessoNegado(AcessoNegadoException exception) {
        return erro(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> campos = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage(),
                        (mensagemExistente, mensagemNova) -> mensagemExistente, LinkedHashMap::new));

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Dados inválidos");
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoInvalido(HttpMessageNotReadableException exception) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou ausente");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInterno(Exception exception) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");
    }

    private ResponseEntity<ErroResposta> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResposta(status.value(), mensagem));
    }
}
