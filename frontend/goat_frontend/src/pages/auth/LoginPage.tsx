import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from '../../components/common/Card';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import { EmailInput } from '../../components/auth/EmailInput';
import { PasswordInput } from '../../components/auth/PasswordInput';
import { useAuth } from '../../context/AuthContext';
import { validateEmail, validatePassword } from '../../utils/validators';
import styles from './AuthPage.module.css';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [loginMethod, setLoginMethod] = useState<'password' | 'otp'>('password');

  const validateForm = () => {
    const newErrors: { email?: string; password?: string } = {};
    
    const emailValidation = validateEmail(email);
    if (!emailValidation.valid) {
      newErrors.email = emailValidation.message;
    }
    
    if (loginMethod === 'password') {
      const passwordValidation = validatePassword(password);
      if (!passwordValidation.valid) {
        newErrors.password = passwordValidation.message;
      }
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    setMessage(null);

    try {
      const result = await login(email, password);
      
      if (result.success) {
        navigate('/dashboard');
      } else {
        setMessage({ type: 'error', text: result.message });
      }
    } catch (error) {
      setMessage({ type: 'error', text: 'Error de conexión con el servidor' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleOTPLogin = () => {
    setLoginMethod('otp');
    navigate('/verify-otp', { state: { email, purpose: 'LOGIN' } });
  };

  return (
    <div className={styles.authPage}>
      <div className={styles.authContainer}>
        <Card className={styles.authCard}>
          <h1 className={styles.authTitle}>Iniciar Sesión</h1>
          
          {message && (
            <Alert
              variant={message.type === 'error' ? 'error' : 'success'}
              onClose={() => setMessage(null)}
            >
              {message.text}
            </Alert>
          )}

          <form onSubmit={handleLogin} className={styles.authForm}>
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

            {loginMethod === 'password' && (
              <PasswordInput
                id="password"
                label="Contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                error={errors.password}
                required
                fullWidth
                autoComplete="current-password"
              />
            )}

            <Button
              type="submit"
              variant="primary"
              fullWidth
              isLoading={isLoading}
              className={styles.submitButton}
            >
              Iniciar Sesión
            </Button>
          </form>

          <div className={styles.authDivider}>
            <span>o</span>
          </div>

          <Button
            variant="outline"
            fullWidth
            onClick={handleOTPLogin}
            className={styles.submitButton}
          >
            Iniciar Sesión con Código OTP
          </Button>

          <div className={styles.authFooter}>
            <p>
              <a href="/reset-password" onClick={(e) => { e.preventDefault(); navigate('/reset-password'); }}>
                ¿Olvidaste tu contraseña?
              </a>
            </p>
            <p>
              ¿No tienes una cuenta?{' '}
              <a href="/register" onClick={(e) => { e.preventDefault(); navigate('/register'); }}>
                Regístrate
              </a>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
};

