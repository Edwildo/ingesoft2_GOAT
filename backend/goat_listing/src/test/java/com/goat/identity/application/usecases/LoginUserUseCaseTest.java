package com.goat.identity.application.usecases;

import com.goat.identity.application.dto.LoginRequest;
import com.goat.identity.application.dto.LoginResponse;
import com.goat.identity.domain.entities.Role;
import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.exceptions.EmailNotConfirmedException;
import com.goat.identity.domain.exceptions.InvalidCredentialsException;
import com.goat.identity.domain.exceptions.UserInactiveException;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.domain.valueobjects.PasswordHash;
import com.goat.identity.ports.PasswordEncoderPort;
import com.goat.identity.ports.TokenGeneratorPort;
import com.goat.identity.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoginUserUseCase.
 * Prueba el flujo de autenticación de usuarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserUseCase Tests")
class LoginUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenGeneratorPort tokenGenerator;

    @InjectMocks
    private LoginUserUseCase loginUserUseCase;

    private User activeUser;
    private User inactiveUser;
    private User unconfirmedUser;
    private UUID userId;
    private String userEmail;
    private String userPassword;
    private String passwordHash;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test@example.com";
        userPassword = "password123";
        passwordHash = "$2a$10$hashedPassword";

        // Usuario activo y con email confirmado
        activeUser = new User(Email.of(userEmail), PasswordHash.of(passwordHash));
        activeUser.setId(userId);
        activeUser.setIsActive(true);
        activeUser.setEmailConfirmed(true);
        
        Role buyerRole = new Role();
        buyerRole.setId(UUID.randomUUID());
        buyerRole.setCode("BUYER");
        buyerRole.setName("Comprador");
        
        Role clientRole = new Role();
        clientRole.setId(UUID.randomUUID());
        clientRole.setCode("CLIENT");
        clientRole.setName("Cliente");
        
        activeUser.setRoles(Arrays.asList(buyerRole, clientRole));

        // Usuario inactivo
        inactiveUser = new User(Email.of("inactive@example.com"), PasswordHash.of(passwordHash));
        inactiveUser.setId(UUID.randomUUID());
        inactiveUser.setIsActive(false);
        inactiveUser.setEmailConfirmed(true);

        // Usuario sin email confirmado
        unconfirmedUser = new User(Email.of("unconfirmed@example.com"), PasswordHash.of(passwordHash));
        unconfirmedUser.setId(UUID.randomUUID());
        unconfirmedUser.setIsActive(true);
        unconfirmedUser.setEmailConfirmed(false);
    }

    @Test
    @DisplayName("Debería autenticar usuario exitosamente con credenciales válidas")
    void shouldAuthenticateUserSuccessfully() {
        // Arrange
        LoginRequest request = new LoginRequest(userEmail, userPassword);
        
        when(userRepository.findByEmail(Email.of(userEmail)))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(userPassword, passwordHash))
                .thenReturn(true);
        when(tokenGenerator.generateToken(
                eq(userId),
                eq(userEmail),
                any(List.class)))
                .thenReturn("jwt-token-12345");

        // Act
        LoginResponse response = loginUserUseCase.execute(request);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-12345", response.getToken());
        assertEquals(userEmail, response.getEmail());
        assertEquals(userId, response.getUserId());
        assertNotNull(response.getRoles());
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains("BUYER"));
        assertTrue(response.getRoles().contains("CLIENT"));

        // Verificar que se llamaron los métodos correctos
        verify(userRepository, times(1)).findByEmail(Email.of(userEmail));
        verify(passwordEncoder, times(1)).matches(userPassword, passwordHash);
        verify(tokenGenerator, times(1)).generateToken(
                eq(userId),
                eq(userEmail),
                anyList());
    }

    @Test
    @DisplayName("Debería lanzar InvalidCredentialsException cuando el usuario no existe")
    void shouldThrowInvalidCredentialsExceptionWhenUserNotFound() {
        // Arrange
        LoginRequest request = new LoginRequest("nonexistent@example.com", userPassword);
        
        when(userRepository.findByEmail(Email.of("nonexistent@example.com")))
                .thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginUserUseCase.execute(request)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(any(UUID.class), anyString(), anyList());
    }

    @Test
    @DisplayName("Debería lanzar UserInactiveException cuando el usuario está inactivo")
    void shouldThrowUserInactiveExceptionWhenUserIsInactive() {
        // Arrange
        LoginRequest request = new LoginRequest("inactive@example.com", userPassword);
        
        when(userRepository.findByEmail(Email.of("inactive@example.com")))
                .thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        UserInactiveException exception = assertThrows(
                UserInactiveException.class,
                () -> loginUserUseCase.execute(request)
        );

        assertEquals("El usuario está inactivo", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(any(UUID.class), anyString(), anyList());
    }

    @Test
    @DisplayName("Debería lanzar EmailNotConfirmedException cuando el email no está confirmado")
    void shouldThrowEmailNotConfirmedExceptionWhenEmailNotConfirmed() {
        // Arrange
        LoginRequest request = new LoginRequest("unconfirmed@example.com", userPassword);
        
        when(userRepository.findByEmail(Email.of("unconfirmed@example.com")))
                .thenReturn(Optional.of(unconfirmedUser));

        // Act & Assert
        EmailNotConfirmedException exception = assertThrows(
                EmailNotConfirmedException.class,
                () -> loginUserUseCase.execute(request)
        );

        assertEquals("El email no ha sido confirmado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenGenerator, never()).generateToken(any(UUID.class), anyString(), anyList());
    }

    @Test
    @DisplayName("Debería lanzar InvalidCredentialsException cuando la contraseña es incorrecta")
    void shouldThrowInvalidCredentialsExceptionWhenPasswordIsIncorrect() {
        // Arrange
        LoginRequest request = new LoginRequest(userEmail, "wrongPassword");
        
        when(userRepository.findByEmail(Email.of(userEmail)))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPassword", passwordHash))
                .thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> loginUserUseCase.execute(request)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(Email.of(userEmail));
        verify(passwordEncoder, times(1)).matches("wrongPassword", passwordHash);
        verify(tokenGenerator, never()).generateToken(any(UUID.class), anyString(), anyList());
    }
}

