"""Implementaciones de persistencia para OTPs."""

from .mongo_confirmed_email_repository import MongoConfirmedEmailRepository
from .mongo_otp_repository import MongoOTPRepository

__all__ = ["MongoConfirmedEmailRepository", "MongoOTPRepository"]

