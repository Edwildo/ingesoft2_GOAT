"""Modelos de persistencia MongoDB para OTPs."""

from .confirmed_email_document import ConfirmedEmailDocument
from .otp_document import OTPDocument

__all__ = ["ConfirmedEmailDocument", "OTPDocument"]

