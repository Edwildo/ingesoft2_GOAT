import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import { OTPInput } from '../../components/auth/OTPInput';
import { useOTP } from '../../hooks/useOTP';
import { authService } from '../../api/auth.service';
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
  
  const [email, setEmail] = useState(stateEmail || '');
  const [purpose, setPurpose] = useState<OTPPurpose>(statePurpose || 'EMAIL_CONFIRMATION');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [expiresIn, setExpiresIn] = useState<number | null>(null);
  const [countdown, setCountdown] = useState<number | null>(null);

  const { generateOTP, verifyOTP } = useOTP();

  useEffect(() => {
    if (!email) {
      // Si no hay email, redirigir al registro o login
      navigate('/register');
      return;
    }

    // Generar OTP automáticamente al cargar
    handleGenerateOTP();
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
    setIsGenerating(true);
    setError(null);
    setMessage(null);

    const success = await generateOTP(email, purpose);
    
    if (success) {
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
    const validation = validateOTP(otp);
    if (!validation.valid) {
      setError(validation.message);
      return;
    }

    setError(null);
    setIsLoading(true);

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
      setMessage({ type: 'error', text: 'Código OTP inválido o expirado' });
    }
    
    setIsLoading(false);
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
              onChange={setOtp}
              onComplete={handleVerifyOTP}
              error={error || undefined}
              disabled={isLoading}
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
                isLoading={isLoading}
                disabled={otp.length !== 6 || countdown === 0}
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

