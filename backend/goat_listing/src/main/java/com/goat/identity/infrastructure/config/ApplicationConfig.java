package com.goat.identity.infrastructure.config;

import com.goat.identity.adapters.persistence.PostgreSQLUserRepository;
import com.goat.identity.application.usecases.ConfirmEmailUseCase;
import com.goat.identity.application.usecases.CreateUserUseCase;
import com.goat.identity.application.usecases.GenerateOtpUseCase;
import com.goat.identity.application.usecases.LoginUserUseCase;
import com.goat.identity.application.usecases.VerifyOtpUseCase;
import com.goat.identity.ports.OtpGenerationPort;
import com.goat.identity.ports.OtpValidationPort;
import com.goat.identity.ports.OtpVerificationPort;
import com.goat.identity.ports.PasswordEncoderPort;
import com.goat.identity.ports.TokenGeneratorPort;
import com.goat.identity.ports.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la aplicación.
 * Define los beans necesarios para la arquitectura hexagonal.
 */
@Configuration
public class ApplicationConfig {
    @Bean
    public LoginUserUseCase loginUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder,
            TokenGeneratorPort tokenGenerator) {
        return new LoginUserUseCase(userRepository, passwordEncoder, tokenGenerator);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoder) {
        return new CreateUserUseCase(userRepository, passwordEncoder);
    }

    @Bean
    public ConfirmEmailUseCase confirmEmailUseCase(
            UserRepository userRepository,
            OtpVerificationPort otpVerificationPort) {
        return new ConfirmEmailUseCase(userRepository, otpVerificationPort);
    }

    @Bean
    public GenerateOtpUseCase generateOtpUseCase(OtpGenerationPort otpGenerationPort) {
        return new GenerateOtpUseCase(otpGenerationPort);
    }

    @Bean
    public VerifyOtpUseCase verifyOtpUseCase(
            OtpValidationPort otpValidationPort,
            UserRepository userRepository) {
        return new VerifyOtpUseCase(otpValidationPort, userRepository);
    }

    @Bean
    public UserRepository userRepository(PostgreSQLUserRepository postgreSQLUserRepository) {
        return postgreSQLUserRepository;
    }
}

