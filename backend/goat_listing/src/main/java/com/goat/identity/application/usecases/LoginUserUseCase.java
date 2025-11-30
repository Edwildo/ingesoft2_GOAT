package com.goat.identity.application.usecases;

import com.goat.identity.application.dto.LoginRequest;
import com.goat.identity.application.dto.LoginResponse;
import com.goat.identity.domain.entities.Role;
import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.exceptions.EmailNotConfirmedException;
import com.goat.identity.domain.exceptions.InvalidCredentialsException;
import com.goat.identity.domain.exceptions.UserInactiveException;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.PasswordEncoderPort;
import com.goat.identity.ports.TokenGeneratorPort;
import com.goat.identity.ports.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para autenticar un usuario.
 * Implementa las reglas de negocio del dominio Identity.
 */
public class LoginUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenGeneratorPort tokenGenerator;

    public LoginUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenGeneratorPort tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public LoginResponse execute(LoginRequest request) {
        Email email = Email.of(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!user.getIsActive()) {
            throw new UserInactiveException("El usuario está inactivo");
        }

        if (!user.getEmailConfirmed()) {
            throw new EmailNotConfirmedException("El email no ha sido confirmado");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash().getValue()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        String token = tokenGenerator.generateToken(user.getId(), user.getEmail().getValue(), roleCodes);

        return new LoginResponse(
                token,
                user.getEmail().getValue(),
                List.copyOf(roleCodes), // Lista inmutable para el record
                user.getId()
        );
    }
}

