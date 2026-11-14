package letsplay.java.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import letsplay.java.dto.Request.*;
import letsplay.java.dto.Response.*;
import letsplay.java.entity.User;
import letsplay.java.repository.UserRepository;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    public void register(RequestRegisterDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is all ready taken");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is all ready taken");
        }
        String hashPassword = passwordEncoder.encode(request.password());
        User newUser = User.builder().username(request.username()).email(request.email())
                .passwordHash(hashPassword).build();

        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public ResponseAuthDto login(String identifier, String password) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, password));

        } catch (LockedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This account has been suspended by an Administrator.");

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        UserDetails authenticatedUser = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(authenticatedUser);
        ResponseAuthDto authDto = mapToDto(token);
        return authDto;
    }

    private ResponseAuthDto mapToDto(String token) {
        return new ResponseAuthDto(token);
    }
}