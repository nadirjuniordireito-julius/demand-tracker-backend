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

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        // Mostrar valor encriptado de "admin" para comparação
       
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + usuario.getPerfil().name())
                .build()
        );
        
        return new AuthResponse(token, UsuarioDTO.fromEntity(usuario), 3600L);
    }
    
    public UsuarioDTO getCurrentUser(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        return UsuarioDTO.fromEntity(usuario);
    }
}
