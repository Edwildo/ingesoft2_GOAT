import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import { OTPInput } from '../../components/auth/OTPInput';
import { useOTP } from '../../hooks/useOTP';
import { OTPPurpose } from '../../types/auth.types';
import { validateOTP } from '../../utils/validators';
import styles from './AuthPage.module.css';

interface VerifyOTPPageProps {
  email?: string;
  purpose?: OTPPurpose;
}

export const VerifyOTPPage: React.FC<VerifyOTPPageProps> = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const stateEmail = location.state?.email as string | undefined;
  const statePurpose = location.state?.purpose as OTPPurpose | undefined;
  const otpAlreadyGenerated = location.state?.otpGenerated as boolean | undefined;
  
  const [email] = useState(stateEmail || '');
  const [purpose] = useState<OTPPurpose>(statePurpose || 'EMAIL_CONFIRMATION');
  const [isGenerating, setIsGenerating] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [expiresIn, setExpiresIn] = useState<number | null>(null);
  const [countdown, setCountdown] = useState<number | null>(null);
  const [isVerifying, setIsVerifying] = useState(false);
  const [hasGeneratedOTP, setHasGeneratedOTP] = useState(otpAlreadyGenerated || false);

  const { otp, setOtp, generateOTP, verifyOTP, isLoading, error } = useOTP();

  useEffect(() => {
    if (!email) {
      // Si no hay email, redirigir al registro o login
      navigate('/register');
      return;
    }

    // Solo generar OTP automáticamente si no se ha generado ya
    if (!hasGeneratedOTP) {
      handleGenerateOTP();
    } else {
      // Si ya se generó, mostrar mensaje y configurar countdown
      setMessage({
        type: 'success',
        text: `Código OTP enviado a ${email}. Por favor, ingrésalo para continuar.`,
      });
      setExpiresIn(5); // 5 minutos por defecto
    }
  }, []);

  useEffect(() => {
    if (expiresIn) {
      setCountdown(expiresIn * 60); // Convertir minutos a segundos
    }
  }, [expiresIn]);

  useEffect(() => {
    if (countdown !== null && countdown > 0) {
      const timer = setInterval(() => {
        setCountdown((prev) => (prev !== null ? prev - 1 : null));
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [countdown]);

  const handleGenerateOTP = async () => {
    // Prevenir múltiples llamadas simultáneas
    if (isGenerating) {
      return;
    }

    setIsGenerating(true);
    setMessage(null);

    const success = await generateOTP(email, purpose);
    
    if (success) {
      setHasGeneratedOTP(true);
      setMessage({
        type: 'success',
        text: `Código OTP enviado a ${email}. Por favor, ingrésalo para continuar.`,
      });
      setExpiresIn(5); // 5 minutos por defecto
    } else {
      setMessage({
        type: 'error',
        text: 'Error al generar código OTP. Por favor, intenta nuevamente.',
      });
    }
    
    setIsGenerating(false);
  };

  const handleVerifyOTP = async () => {
    // Prevenir múltiples llamadas simultáneas
    if (isVerifying || isLoading) {
      return;
    }

    // Normalizar el OTP antes de validar
    const normalizedOTP = otp.replace(/\D/g, '').slice(0, 6);
    
    const validation = validateOTP(normalizedOTP);
    if (!validation.valid) {
      setMessage({ type: 'error', text: validation.message || 'El código OTP debe tener 6 dígitos' });
      return;
    }

    setIsVerifying(true);
    setMessage(null);

    const success = await verifyOTP(email, purpose);
    
    if (success) {
      setMessage({ type: 'success', text: 'Código OTP verificado correctamente' });
      
      // Redirigir según el propósito
      setTimeout(() => {
        if (purpose === 'EMAIL_CONFIRMATION') {
          navigate('/login', { state: { message: 'Email confirmado exitosamente' } });
        } else if (purpose === 'LOGIN') {
          // Para login con OTP, necesitarías un endpoint adicional o redirigir a login tradicional
          navigate('/login', { state: { message: 'OTP verificado. Por favor, inicia sesión.' } });
        } else if (purpose === 'RESET_PASSWORD') {
          navigate('/reset-password', { state: { email, verified: true } });
        } else {
          navigate('/dashboard');
        }
      }, 1500);
    } else {
      // El error ya está manejado por el hook useOTP
      setMessage({ type: 'error', text: error || 'Código OTP inválido o expirado' });
    }
    
    setIsVerifying(false);
  };

  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const getPurposeTitle = (): string => {
    switch (purpose) {
      case 'EMAIL_CONFIRMATION':
        return 'Confirmar Email';
      case 'LOGIN':
        return 'Iniciar Sesión con OTP';
      case 'RESET_PASSWORD':
        return 'Recuperar Contraseña';
      default:
        return 'Verificar Código';
    }
  };

  return (
    <div className={styles.authPage}>
      <div className={styles.authContainer}>
        <Card className={styles.authCard}>
          <h1 className={styles.authTitle}>{getPurposeTitle()}</h1>
          
          {message && (
            <Alert
              variant={message.type === 'error' ? 'error' : 'success'}
              onClose={() => setMessage(null)}
            >
              {message.text}
            </Alert>
          )}

          <div className={styles.otpContainer}>
            <p className={styles.otpInstructions}>
              Ingresa el código de 6 dígitos enviado a <strong>{email}</strong>
            </p>

            <OTPInput
              value={otp}
              onChange={(value) => {
                setOtp(value);
                setMessage(null); // Limpiar mensajes al cambiar el OTP
              }}
              onComplete={undefined}
              error={error || undefined}
              disabled={isLoading || isVerifying}
            />

            {countdown !== null && countdown > 0 && (
              <p className={styles.countdown}>
                El código expira en: <strong>{formatTime(countdown)}</strong>
              </p>
            )}

            {countdown === 0 && (
              <Alert variant="warning">
                El código ha expirado. Por favor, solicita uno nuevo.
              </Alert>
            )}

            <div className={styles.otpActions}>
              <Button
                variant="primary"
                onClick={handleVerifyOTP}
                isLoading={isLoading || isVerifying}
                disabled={otp.replace(/\D/g, '').length !== 6 || countdown === 0 || isVerifying || isLoading}
                fullWidth
              >
                Verificar Código
              </Button>

              <Button
                variant="outline"
                onClick={handleGenerateOTP}
                isLoading={isGenerating}
                fullWidth
                className={styles.resendButton}
              >
                Reenviar Código
              </Button>
            </div>
          </div>

          <div className={styles.authFooter}>
            <p>
              <a href="#" onClick={(e) => { e.preventDefault(); navigate(-1); }}>
                Volver
              </a>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};

