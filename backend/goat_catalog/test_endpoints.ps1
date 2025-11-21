# Script PowerShell para probar los endpoints del servicio GOAT Catalog
# Puerto por defecto: 8082

$BaseUrl = "http://localhost:8082"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🧪 Testing GOAT Catalog Service Endpoints" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Health Check
Write-Host "1️⃣  Health Check" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/health" -Method GET -ContentType "application/json"
    Write-Host "✅ Status: OK" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2. Generar OTP para REGISTER
Write-Host "2️⃣  Generar OTP para REGISTER" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
$generateBody = @{
    email = "usuario@example.com"
    purpose = "REGISTER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/otp" -Method POST -Body $generateBody -ContentType "application/json"
    Write-Host "✅ OTP generado correctamente" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
    $generatedOtp = "123456"  # En producción, esto vendría del email o logs
    Write-Host "⚠️  NOTA: Por ahora debes obtener el OTP de MongoDB o logs" -ForegroundColor Yellow
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalle: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# 3. Validar OTP (Reemplazar 123456 con el OTP real)
Write-Host "3️⃣  Validar OTP" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
Write-Host "⚠️  NOTA: Reemplaza '123456' con el OTP real generado anteriormente" -ForegroundColor Yellow
$verifyBody = @{
    email = "usuario@example.com"
    otp = "123456"
    purpose = "REGISTER"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/verify" -Method POST -Body $verifyBody -ContentType "application/json"
    Write-Host "✅ OTP válido" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalle: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# 4. Generar OTP para LOGIN
Write-Host "4️⃣  Generar OTP para LOGIN" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
$loginBody = @{
    email = "usuario@example.com"
    purpose = "LOGIN"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/otp" -Method POST -Body $loginBody -ContentType "application/json"
    Write-Host "✅ OTP generado correctamente" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalle: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# 5. Verificar estado de confirmación de email
Write-Host "5️⃣  Verificar Estado de Confirmación de Email" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/verify-email-status?email=usuario@example.com" -Method GET -ContentType "application/json"
    Write-Host "✅ Estado verificado" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalle: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# 6. Confirmar email (verificar estado)
Write-Host "6️⃣  Confirmar Email (Verificar Estado)" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/auth/confirm-email?email=usuario@example.com" -Method GET -ContentType "application/json"
    Write-Host "✅ Estado verificado" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Detalle: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ Pruebas completadas" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan

