package letsplay.java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import letsplay.java.dto.Request.RequestRegisterDto;
import letsplay.java.dto.Request.RequestUserDto;
import letsplay.java.dto.Response.*;
import letsplay.java.entity.User;
import letsplay.java.repository.ProductRepository;
import letsplay.java.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<ResponseUserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> mapToDto(user)).toList();
    }

    public ResponseUserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToDto(user);
    }

    public ResponseUserDto createUser(RequestRegisterDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }
        String hashPassword = passwordEncoder.encode(request.password());
        User newUser = User.builder().username(request.username()).email(request.email())
                .passwordHash(hashPassword).build();

        userRepository.save(newUser);
        return mapToDto(newUser);
    }

    public ResponseUserDto updateUser(String id, RequestUserDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setRole(request.role());
        userRepository.save(user);
        return mapToDto(user);
    }

    @Transactional
    public String deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        productRepository.deleteAllByUserId(id);
        userRepository.delete(user);
        return "User deleted successfully";
    }

    private ResponseUserDto mapToDto(User user) {
        return new ResponseUserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}