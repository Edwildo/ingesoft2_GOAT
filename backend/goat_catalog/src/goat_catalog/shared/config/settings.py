"""Configuración de la aplicación desde variables de entorno."""

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Configuración de la aplicación.

    Todas las configuraciones pueden ser sobrescritas mediante variables de entorno
    o un archivo .env en la raíz del proyecto.
    """

    # MongoDB Configuration
    mongodb_uri: str = Field(
        default="mongodb://localhost:27017",
        description="URI de conexión a MongoDB. "
        "Para MongoDB Local: mongodb://localhost:27017. "
        "Para MongoDB Atlas: mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority",
        examples=[
            "mongodb://localhost:27017",
            "mongodb+srv://user:pass@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority",
        ],
    )
    mongodb_database: str = Field(
        default="auth",
        description="Nombre de la base de datos MongoDB",
        examples=["auth"],
    )

    # OTP Configuration
    otp_expiration_minutes: int = 5
    otp_max_attempts: int = 3
    otp_length: int = 6

    # Email Configuration
    smtp_host: str = "smtp.example.com"
    smtp_port: int = 587
    smtp_user: str = ""
    smtp_password: str = ""
    email_from: str = "noreply@goat.com"

    # Security
    otp_hash_secret: str = "default_secret_change_in_production"

    # Server
    server_port: int = 8082
    environment: str = "development"

    # CORS
    cors_origins: str = "http://localhost:3000,http://localhost:5173"

    # Rate Limiting
    rate_limit_per_minute: int = Field(
        default=60,
        description="Límite global de requests por minuto por IP",
    )
    rate_limit_otp_per_email: int = Field(
        default=5,
        description="Límite de generación de OTPs por email por minuto",
    )
    rate_limit_otp_per_ip: int = Field(
        default=10,
        description="Límite de generación de OTPs por IP por minuto",
    )
    rate_limit_verify_per_email: int = Field(
        default=10,
        description="Límite de verificaciones de OTP por email por minuto",
    )
    rate_limit_storage_uri: str = Field(
        default="memory://",
        description="URI de almacenamiento para rate limiting (memory:// para desarrollo, redis:// para producción)",
    )

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    @property
    def cors_origins_list(self) -> list[str]:
        """Retorna lista de orígenes CORS."""
        return [origin.strip() for origin in self.cors_origins.split(",")]


_settings: Settings | None = None


def get_settings() -> Settings:
    """Obtiene la instancia singleton de configuración."""
    global _settings
    if _settings is None:
        _settings = Settings()
    return _settings

