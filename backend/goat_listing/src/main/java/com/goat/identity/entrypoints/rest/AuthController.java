package com.goat.identity.entrypoints.rest;

import com.goat.identity.application.dto.CreateUserRequest;
import com.goat.identity.application.dto.CreateUserResponse;
import com.goat.identity.application.dto.GenerateOtpRequest;
import com.goat.identity.application.dto.GenerateOtpResponse;
import com.goat.identity.application.dto.LoginRequest;
import com.goat.identity.application.dto.LoginResponse;
import com.goat.identity.application.dto.VerifyOtpRequest;
import com.goat.identity.application.dto.VerifyOtpResponse;
import com.goat.identity.application.usecases.ConfirmEmailUseCase;
import com.goat.identity.application.usecases.CreateUserUseCase;
import com.goat.identity.application.usecases.GenerateOtpUseCase;
import com.goat.identity.application.usecases.LoginUserUseCase;
import com.goat.identity.application.usecases.VerifyOtpUseCase;
import com.goat.identity.domain.valueobjects.Email;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operaciones de autenticación.
 * Entrypoint de la arquitectura hexagonal.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final LoginUserUseCase loginUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final ConfirmEmailUseCase confirmEmailUseCase;
    private final GenerateOtpUseCase generateOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;

    public AuthController(
            LoginUserUseCase loginUserUseCase,
            CreateUserUseCase createUserUseCase,
            ConfirmEmailUseCase confirmEmailUseCase,
            GenerateOtpUseCase generateOtpUseCase,
            VerifyOtpUseCase verifyOtpUseCase) {
        this.loginUserUseCase = loginUserUseCase;
        this.createUserUseCase = createUserUseCase;
        this.confirmEmailUseCase = confirmEmailUseCase;
        this.generateOtpUseCase = generateOtpUseCase;
        this.verifyOtpUseCase = verifyOtpUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        CreateUserResponse response = createUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<Map<String, Object>> confirmEmail(@RequestParam String email) {
        try {
            Email emailValueObject = Email.of(email);
            boolean confirmed = confirmEmailUseCase.execute(emailValueObject);

            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("confirmed", confirmed);
            response.put("message", confirmed 
                    ? "Email confirmado exitosamente" 
                    : "El email aún no ha sido confirmado (OTP no verificado)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error al verificar el email");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/otp")
    public ResponseEntity<GenerateOtpResponse> generateOtp(@Valid @RequestBody GenerateOtpRequest request) {
        GenerateOtpResponse response = generateOtpUseCase.execute(request);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = verifyOtpUseCase.execute(request);
        HttpStatus status = response.isValid() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}

