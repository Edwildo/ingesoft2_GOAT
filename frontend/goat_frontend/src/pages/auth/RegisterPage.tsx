import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import { EmailInput } from '../../components/auth/EmailInput';
import { PasswordInput } from '../../components/auth/PasswordInput';
import { useAuth } from '../../context/AuthContext';
import { authService } from '../../api/auth.service';
import { validateEmail, validatePassword } from '../../utils/validators';
import styles from './AuthPage.module.css';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [step, setStep] = useState<'register' | 'verify'>('register');
  const [userId, setUserId] = useState<string | null>(null);

  const validateForm = () => {
    const newErrors: { email?: string; password?: string } = {};
    
    const emailValidation = validateEmail(email);
    if (!emailValidation.valid) {
      newErrors.email = emailValidation.message;
    }
    
    const passwordValidation = validatePassword(password);
    if (!passwordValidation.valid) {
      newErrors.password = passwordValidation.message;
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setMessage(null);

    try {
      // Registrar usuario
      const registerResult = await register(email, password);
      
      if (!registerResult.success) {
        setMessage({ type: 'error', text: registerResult.message });
        setIsLoading(false);
        return;
      }

      setUserId(registerResult.userId || null);

      // Generar OTP
      const otpResult = await authService.generateOTP({
        email,
        purpose: 'EMAIL_CONFIRMATION',
      });

      if (!otpResult.success || !otpResult.data?.success) {
        setMessage({
          type: 'error',
          text: otpResult.error?.message || 'Error al generar código OTP',
        });
        setIsLoading(false);
        return;
      }

      setMessage({
        type: 'success',
        text: `Código OTP enviado a ${email}. Por favor, ingrésalo para confirmar tu email.`,
      });
      setStep('verify');
    } catch (error) {
      setMessage({ type: 'error', text: 'Error de conexión con el servidor' });
    } finally {
      setIsLoading(false);
    }
  };

  if (step === 'verify') {
    navigate('/verify-otp', { state: { email, purpose: 'EMAIL_CONFIRMATION' } });
    return null;
  }

  return (
    <div className={styles.authPage}>
      <div className={styles.authContainer}>
        <Card className={styles.authCard}>
          <h1 className={styles.authTitle}>Registro</h1>
          
          {message && (
            <Alert
              variant={message.type === 'error' ? 'error' : 'success'}
              onClose={() => setMessage(null)}
            >
              {message.text}
            </Alert>
          )}

          <form onSubmit={handleRegister} className={styles.authForm}>
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

            <PasswordInput
              id="password"
              label="Contraseña"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              required
              fullWidth
              showStrengthIndicator
              autoComplete="new-password"
            />

            <Button
              type="submit"
              variant="primary"
              fullWidth
              isLoading={isLoading}
              className={styles.submitButton}
            >
              Registrarse
            </Button>
          </form>

          <div className={styles.authFooter}>
            <p>
              ¿Ya tienes una cuenta?{' '}
              <a href="/login" onClick={(e) => { e.preventDefault(); navigate('/login'); }}>
                Inicia sesión
              </a>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};

