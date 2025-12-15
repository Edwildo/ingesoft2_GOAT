"""Punto de entrada principal de la aplicación."""

from fastapi import FastAPI

from .bootstrap.app_factory import create_app
from .cart.entrypoints.rest.cart_controller import router as cart_router
from .catalog.entrypoints.rest import (
    brand_router,
    category_router,
    collection_router,
    sneaker_router,
)
from .shared.entrypoints.health_controller import router as health_router
from .tokens.otps.entrypoints.rest import router as otp_router
from .notifications.entrypoints.rest.notification_controller import router as notification_router

app: FastAPI = create_app()

# Registrar routers
app.include_router(health_router)
app.include_router(otp_router)
app.include_router(notification_router)

# Catalog routers
app.include_router(sneaker_router)
app.include_router(brand_router)
app.include_router(category_router)
app.include_router(collection_router)

# Cart router
app.include_router(cart_router)
