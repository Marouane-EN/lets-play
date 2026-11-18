package letsplay.java.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import letsplay.java.dto.Request.*;
import letsplay.java.dto.Response.*;
import letsplay.java.service.UserService;

@RequestMapping("/users")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<ResponseUserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostAuthorize("hasRole('ADMIN') or returnObject.body.id() == principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        ResponseUserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody RequestRegisterDto request) {
        ResponseUserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id,
            @Valid @RequestBody RequestUserDto request) {
        ResponseUserDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        String message = userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", message));
    }
}