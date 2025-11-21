"""Domain Service para generar códigos OTP."""

import random
import string
import sys
from pathlib import Path

# Agregar src al path para importaciones absolutas
src_path = Path(__file__).parent.parent.parent.parent.parent
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.shared.config import get_settings


class OTPGeneratorService:
    """Servicio de dominio para generar códigos OTP."""

    @staticmethod
    def generate() -> str:
        """Genera un código OTP numérico.

        Returns:
            Código OTP de 6 dígitos por defecto
        """
        settings = get_settings()
        digits = string.digits
        return "".join(random.choice(digits) for _ in range(settings.otp_length))

