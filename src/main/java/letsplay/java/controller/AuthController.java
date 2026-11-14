package letsplay.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import letsplay.java.dto.Request.*;
import letsplay.java.dto.Response.*;
import letsplay.java.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody RequestLoginDto request) {
        ResponseAuthDto authDto = authService.login(request.identifier(), request.password());
        return ResponseEntity.ok(authDto);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RequestRegisterDto request) {
        authService.register(request);
        ResponseAuthDto authDto = authService.login(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(authDto);
    }

}