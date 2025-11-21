"""Punto de entrada principal de la aplicación."""

from fastapi import FastAPI

from .bootstrap.app_factory import create_app
from .shared.entrypoints.health_controller import router as health_router
from .tokens.otps.entrypoints.rest import router as otp_router

app: FastAPI = create_app()

# Registrar routers
app.include_router(health_router)
app.include_router(otp_router)
