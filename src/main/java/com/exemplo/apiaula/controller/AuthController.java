package com.exemplo.apiaula.controller;

import com.exemplo.apiaula.dto.LoginRequest;
import com.exemplo.apiaula.dto.LoginResponse;
import com.exemplo.apiaula.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse entrar(@RequestBody @Valid LoginRequest request) {
        return service.entrar(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(@RequestHeader(value = "Authorization", required = false) String cabecalho) {
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            service.sair(cabecalho.substring("Bearer ".length()));
        }
    }
}
