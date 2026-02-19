package com.demandtracker.service;

import com.demandtracker.dto.AuthResponse;
import com.demandtracker.dto.LoginRequest;
import com.demandtracker.dto.UsuarioDTO;
import com.demandtracker.entity.Usuario;
import com.demandtracker.repository.UsuarioRepository;
import com.demandtracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import jakarta.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletResponse response;

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        String token = jwtService.generateAccessToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getPerfil().name())
                .build()
        );

        // Gera access e refresh tokens
        String accessToken = jwtService.generateAccessToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getPerfil().name())
                .build()
        );

        String refreshToken = jwtService.generateRefreshToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getPerfil().name())
                .build()
        );

        // Cria cookie HttpOnly para refresh token
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // true em produção com HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 dias
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        
        // return new AuthResponse(token, UsuarioDTO.fromEntity(usuario), 3600L);
        return new AuthResponse(accessToken, UsuarioDTO.fromEntity(usuario), 3600L);
    }
    
    public UsuarioDTO getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return UsuarioDTO.fromEntity(usuario);
    }
}
