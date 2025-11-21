#!/bin/bash
# Script para probar los endpoints del servicio GOAT Catalog
# Puerto por defecto: 8082

BASE_URL="http://localhost:8082"

echo "=========================================="
echo "🧪 Testing GOAT Catalog Service Endpoints"
echo "=========================================="
echo ""

# 1. Health Check
echo "1️⃣  Health Check"
echo "------------------------------------------"
curl -X GET "${BASE_URL}/health" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# 2. Generar OTP para REGISTER
echo "2️⃣  Generar OTP para REGISTER"
echo "------------------------------------------"
curl -X POST "${BASE_URL}/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "REGISTER"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 3. Generar OTP para LOGIN
echo "3️⃣  Generar OTP para LOGIN"
echo "------------------------------------------"
curl -X POST "${BASE_URL}/api/auth/otp" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "purpose": "LOGIN"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 4. Validar OTP (Reemplazar 123456 con el OTP real generado)
echo "4️⃣  Validar OTP"
echo "------------------------------------------"
echo "⚠️  NOTA: Reemplaza '123456' con el OTP real generado anteriormente"
curl -X POST "${BASE_URL}/api/auth/verify" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "otp": "123456",
    "purpose": "REGISTER"
  }' \
  -w "\nStatus: %{http_code}\n\n"

# 5. Verificar estado de confirmación de email
echo "5️⃣  Verificar Estado de Confirmación de Email"
echo "------------------------------------------"
curl -X GET "${BASE_URL}/api/auth/verify-email-status?email=usuario@example.com" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

# 6. Confirmar email (verificar estado)
echo "6️⃣  Confirmar Email (Verificar Estado)"
echo "------------------------------------------"
curl -X GET "${BASE_URL}/api/auth/confirm-email?email=usuario@example.com" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n\n"

echo "=========================================="
echo "✅ Pruebas completadas"
echo "=========================================="

