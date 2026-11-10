package letsplay.java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import letsplay.java.dto.Response.UserDto;
import letsplay.java.entity.User;
import letsplay.java.repository.ProductRepository;
import letsplay.java.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> mapToDto(user)).toList();
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return mapToDto(user);
    }

    public UserDto updateUser(String id, letsplay.java.dto.Request.UserDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Username already taken");
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

    private UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}