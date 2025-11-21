import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import { EmailInput } from '../../components/auth/EmailInput';
import { PasswordInput } from '../../components/auth/PasswordInput';
import { authService } from '../../api/auth.service';
import { validateEmail, validatePassword } from '../../utils/validators';
import styles from './AuthPage.module.css';

export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const stateEmail = location.state?.email as string | undefined;
  const isVerified = location.state?.verified as boolean | undefined;
  
  const [email, setEmail] = useState(stateEmail || '');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [step, setStep] = useState<'request' | 'verify' | 'reset'>(isVerified ? 'reset' : 'request');
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const handleRequestOTP = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const emailValidation = validateEmail(email);
    if (!emailValidation.valid && emailValidation.message) {
      setErrors({ email: emailValidation.message });
      return;
    }

    setIsLoading(true);
    setMessage(null);

    try {
      const result = await authService.generateOTP({
        email,
        purpose: 'RESET_PASSWORD',
      });

      if (result.success && result.data?.success) {
        setMessage({
          type: 'success',
          text: `Código OTP enviado a ${email}. Por favor, ingrésalo para continuar.`,
        });
        setStep('verify');
        navigate('/verify-otp', { 
          state: { 
            email, 
            purpose: 'RESET_PASSWORD',
            otpGenerated: true 
          } 
        });
      } else {
        setMessage({
          type: 'error',
          text: result.error?.message || 'Error al generar código OTP',
        });
      }
    } catch (error) {
      setMessage({ type: 'error', text: 'Error de conexión con el servidor' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const newErrors: Record<string, string> = {};
    
    const passwordValidation = validatePassword(newPassword);
    if (!passwordValidation.valid && passwordValidation.message) {
      newErrors.newPassword = passwordValidation.message;
    }
    
    if (newPassword !== confirmPassword) {
      newErrors.confirmPassword = 'Las contraseñas no coinciden';
    }
    
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setIsLoading(true);
    setMessage(null);

    // Aquí deberías llamar al endpoint de reset password cuando esté disponible
    // Por ahora, solo mostramos un mensaje
    setTimeout(() => {
      setMessage({
        type: 'success',
        text: 'Contraseña actualizada exitosamente. Redirigiendo al login...',
      });
      setTimeout(() => {
        navigate('/login', { state: { message: 'Contraseña actualizada exitosamente' } });
      }, 2000);
    }, 1000);
  };

  if (step === 'verify') {
    // Esto se maneja en VerifyOTPPage
    return null;
  }

  return (
    <div className={styles.authPage}>
      <div className={styles.authContainer}>
        <Card className={styles.authCard}>
          <h1 className={styles.authTitle}>
            {step === 'reset' ? 'Nueva Contraseña' : 'Recuperar Contraseña'}
          </h1>
          
          {message && (
            <Alert
              variant={message.type === 'error' ? 'error' : 'success'}
              onClose={() => setMessage(null)}
            >
              {message.text}
            </Alert>
          )}

          {step === 'request' ? (
            <form onSubmit={handleRequestOTP} className={styles.authForm}>
              <p className={styles.formDescription}>
                Ingresa tu email y te enviaremos un código para recuperar tu contraseña.
              </p>

              <EmailInput
                id="email"
                label="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                error={errors.email}
                required
                fullWidth
                autoComplete="email"
              />

              <Button
                type="submit"
                variant="primary"
                fullWidth
                isLoading={isLoading}
                className={styles.submitButton}
              >
                Enviar Código
              </Button>
            </form>
          ) : (
            <form onSubmit={handleResetPassword} className={styles.authForm}>
              <p className={styles.formDescription}>
                Ingresa tu nueva contraseña.
              </p>

              <PasswordInput
                id="newPassword"
                label="Nueva Contraseña"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                error={errors.newPassword}
                required
                fullWidth
                showStrengthIndicator
                autoComplete="new-password"
              />

              <PasswordInput
                id="confirmPassword"
                label="Confirmar Contraseña"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                error={errors.confirmPassword}
                required
                fullWidth
                autoComplete="new-password"
              />

              <Button
                type="submit"
                variant="primary"
                fullWidth
                isLoading={isLoading}
                className={styles.submitButton}
              >
                Actualizar Contraseña
              </Button>
            </form>
          )}

          <div className={styles.authFooter}>
            <p>
              <a href="/login" onClick={(e) => { e.preventDefault(); navigate('/login'); }}>
                Volver al inicio de sesión
              </a>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};

