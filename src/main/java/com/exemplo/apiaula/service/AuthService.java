package com.exemplo.apiaula.service;

import com.exemplo.apiaula.dto.LoginRequest;
import com.exemplo.apiaula.dto.LoginResponse;
import com.exemplo.apiaula.dto.UsuarioResponse;
import com.exemplo.apiaula.entity.Usuario;
import com.exemplo.apiaula.exception.NaoAutenticadoException;
import com.exemplo.apiaula.repository.UsuarioRepository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login simples para fins didáticos: cada login gera um token aleatório guardado em memória.
 * Ao reiniciar a aplicação, todos precisam entrar de novo.
 * Em produção usaríamos JWT / Spring Security.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Long> sessoes = new ConcurrentHashMap<>();

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse entrar(LoginRequest request) {
        // A mesma mensagem para e-mail errado e senha errada, para não revelar quais e-mails existem
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .filter(u -> passwordEncoder.matches(request.senha(), u.getSenha()))
                .orElseThrow(() -> new NaoAutenticadoException("E-mail ou senha inválidos"));

        String token = UUID.randomUUID().toString();
        sessoes.put(token, usuario.getId());

        UsuarioResponse resposta = new UsuarioResponse(usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getIdade(), usuario.getAtivo());
        return new LoginResponse(token, resposta);
    }

    public void sair(String token) {
        sessoes.remove(token);
    }

    public Long buscarUsuarioIdPorToken(String token) {
        Long usuarioId = sessoes.get(token);
        if (usuarioId == null) {
            throw new NaoAutenticadoException("Sessão inválida ou expirada. Faça login novamente");
        }
        return usuarioId;
    }
}
