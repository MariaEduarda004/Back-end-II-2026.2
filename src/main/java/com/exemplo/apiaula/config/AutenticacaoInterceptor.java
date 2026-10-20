package com.exemplo.apiaula.config;

import com.exemplo.apiaula.exception.NaoAutenticadoException;
import com.exemplo.apiaula.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Roda antes dos controllers protegidos. Lê o cabeçalho "Authorization: Bearer <token>",
 * descobre quem está logado e deixa o id disponível na requisição ("usuarioLogadoId").
 */
@Component
public class AutenticacaoInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO_LOGADO = "usuarioLogadoId";
    private static final String PREFIXO = "Bearer ";

    private final AuthService authService;

    public AutenticacaoInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Cadastrar um novo usuário (POST /usuarios) é a única rota protegida que fica pública
        if (ehCadastroDeUsuario(request)) {
            return true;
        }

        String cabecalho = request.getHeader("Authorization");
        if (cabecalho == null || !cabecalho.startsWith(PREFIXO)) {
            throw new NaoAutenticadoException("Faça login para continuar");
        }

        Long usuarioId = authService.buscarUsuarioIdPorToken(cabecalho.substring(PREFIXO.length()));
        request.setAttribute(ATRIBUTO_USUARIO_LOGADO, usuarioId);
        return true;
    }

    private boolean ehCadastroDeUsuario(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) && "/usuarios".equals(request.getRequestURI());
    }
}
