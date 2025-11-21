package com.goat.identity.application.usecases;

import com.goat.identity.application.dto.CreateUserRequest;
import com.goat.identity.application.dto.CreateUserResponse;
import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.exceptions.EmailAlreadyExistsException;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.domain.valueobjects.PasswordHash;
import com.goat.identity.ports.PasswordEncoderPort;
import com.goat.identity.ports.UserRepository;

/**
 * Caso de uso para crear un nuevo usuario.
 * Implementa las reglas de negocio del dominio Identity.
 */
public class CreateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CreateUserResponse execute(CreateUserRequest request) {
        Email email = Email.of(request.getEmail());

        // Validar que el email no exista (regla de negocio: email único)
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        // Hashear la contraseña (regla de negocio: nunca almacenar contraseñas en texto plano)
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        PasswordHash passwordHash = PasswordHash.of(hashedPassword);

        // Crear usuario (el constructor ya inicializa emailConfirmed=false e isActive=true)
        User user = new User(email, passwordHash);

        // Guardar usuario
        User savedUser = userRepository.save(user);

        // Retornar respuesta (sin exponer el hash de contraseña)
        return new CreateUserResponse(
                savedUser.getId(),
                savedUser.getEmail().getValue(),
                savedUser.getEmailConfirmed(),
                savedUser.getIsActive()
        );
    }
}

