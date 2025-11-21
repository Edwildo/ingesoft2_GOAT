"""Test de integración básico para el flujo de OTP."""

import sys
from pathlib import Path

import pytest
from httpx import ASGITransport, AsyncClient

# Agregar src al path
src_path = Path(__file__).parent.parent.parent.parent / "src"
if str(src_path) not in sys.path:
    sys.path.insert(0, str(src_path))

from goat_catalog.main import app


class TestOTPFlow:
    """Test de integración básico para el flujo de OTP."""

    @pytest.mark.asyncio
    async def test_health_check(self) -> None:
        """Test que verifica el endpoint de health check."""
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            response = await client.get("/health")

            assert response.status_code == 200
            data = response.json()
            assert data["status"] == "ok"
            assert data["service"] == "goat-catalog-service"
            assert data["version"] == "1.0.0"

    @pytest.mark.asyncio
    async def test_generate_otp_endpoint_exists(self) -> None:
        """Test que verifica que el endpoint de generar OTP existe y responde."""
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            # Este test solo verifica que el endpoint existe
            # En un entorno real con MongoDB, verificaríamos el flujo completo
            response = await client.post(
                "/api/auth/otp",
                json={
                    "email": "test-flow@example.com",
                    "purpose": "REGISTER",
                },
            )

            # El endpoint debe responder (200 o 500 según conexión a MongoDB)
            # Solo verificamos que no sea 404 (endpoint no encontrado)
            assert response.status_code != 404

    @pytest.mark.asyncio
    async def test_verify_email_status_endpoint_exists(self) -> None:
        """Test que verifica que el endpoint de verificar estado de email existe."""
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            # Este test solo verifica que el endpoint existe
            response = await client.get(
                "/api/auth/verify-email-status?email=newuser@example.com"
            )

            # El endpoint debe responder (200 o 500 según conexión a MongoDB)
            # Solo verificamos que no sea 404 (endpoint no encontrado)
            assert response.status_code != 404

    @pytest.mark.asyncio
    async def test_confirm_email_endpoint_exists(self) -> None:
        """Test que verifica que el endpoint de confirmar email existe."""
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as client:
            # Este test solo verifica que el endpoint existe
            response = await client.get(
                "/api/auth/confirm-email?email=newuser@example.com"
            )

            # El endpoint debe responder (200 o 500 según conexión a MongoDB)
            # Solo verificamos que no sea 404 (endpoint no encontrado)
            assert response.status_code != 404

