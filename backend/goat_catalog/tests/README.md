# 🧪 Tests - GOAT Catalog Service

## Estructura de Tests

```
tests/
├── unit/                    # Tests unitarios
│   └── tokens/
│       └── otps/
│           ├── domain/      # Tests de entidades y value objects
│           └── application/ # Tests de casos de uso
└── integration/             # Tests de integración
    └── tokens/
        └── otps/           # Tests de flujos completos
```

## Ejecutar Tests

### Todos los tests
```bash
pytest
```

### Tests unitarios solamente
```bash
pytest tests/unit/
```

### Tests de integración solamente
```bash
pytest tests/integration/
```

### Tests con cobertura
```bash
pytest --cov=src --cov-report=html
```

### Tests específicos
```bash
# Test de una clase específica
pytest tests/unit/tokens/otps/domain/test_otp_token.py

# Test de un método específico
pytest tests/unit/tokens/otps/domain/test_otp_token.py::TestOTPToken::test_create_otp_token
```

## Tests Implementados

### Tests Unitarios

1. **Domain Layer**
   - `test_otp_token.py` - Tests de la entidad OTPToken
   - `test_email.py` - Tests del Value Object Email

2. **Application Layer**
   - `test_generate_otp_use_case.py` - Tests del caso de uso GenerateOTP
   - `test_validate_otp_use_case.py` - Tests del caso de uso ValidateOTP

### Tests de Integración

1. **Flujos completos**
   - `test_otp_flow.py` - Test del flujo completo de OTP

## Notas

- Los tests unitarios usan **mocks** para no depender de MongoDB
- Los tests de integración requieren MongoDB (pueden fallar si no hay conexión)
- Todos los tests siguen el patrón **Arrange-Act-Assert (AAA)**

