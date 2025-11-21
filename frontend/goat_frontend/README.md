# GOAT Frontend - Sistema de Autenticación

Frontend de GOAT construido con React, TypeScript y Vite. Implementa un sistema completo de autenticación con registro, login, verificación de email mediante OTP y recuperación de contraseña.

## 🚀 Características

- ✅ Registro de usuarios con validación
- ✅ Login tradicional (email + contraseña)
- ✅ Login con código OTP
- ✅ Verificación de email mediante OTP
- ✅ Recuperación de contraseña
- ✅ Diseño estilo periódico (clean, organizado, tipografía serif)
- ✅ Componentes reutilizables y modulares
- ✅ TypeScript para type safety
- ✅ Manejo de estado con Context API
- ✅ Validación de formularios
- ✅ Manejo de errores centralizado

## 📋 Requisitos Previos

- Node.js 18+ 
- npm o yarn
- Servicio Java corriendo en `http://localhost:8081`
- Servicio Python corriendo en `http://localhost:8082`

## 🛠️ Instalación

1. Instalar dependencias:
```bash
npm install
```

2. Iniciar el servidor de desarrollo:
```bash
npm run dev
```

3. Abrir en el navegador:
```
http://localhost:3000
```

## 📁 Estructura del Proyecto

```
goat_frontend/
├── src/
│   ├── api/              # Servicios de API
│   ├── components/       # Componentes reutilizables
│   │   ├── common/       # Componentes comunes
│   │   └── auth/         # Componentes de autenticación
│   ├── context/          # Context API
│   ├── hooks/            # Custom hooks
│   ├── pages/            # Páginas/Vistas
│   ├── types/            # Tipos TypeScript
│   ├── utils/            # Utilidades
│   └── styles/           # Estilos globales
├── public/
└── package.json
```

## 🎨 Estilo

El proyecto utiliza un diseño estilo periódico con:
- Tipografía serif para títulos
- Paleta de grises
- Diseño limpio y organizado
- Bordes y separadores claros
- Estructura legible y profesional

## 🔐 Flujos de Autenticación

### Registro
1. Usuario ingresa email y contraseña
2. Sistema crea el usuario
3. Se genera y envía código OTP
4. Usuario ingresa OTP para confirmar email
5. Redirección a login

### Login Tradicional
1. Usuario ingresa email y contraseña
2. Sistema valida credenciales
3. Se genera token JWT
4. Redirección a dashboard

### Login con OTP
1. Usuario ingresa email
2. Se genera y envía código OTP
3. Usuario ingresa OTP
4. Se valida y genera token
5. Redirección a dashboard

### Recuperación de Contraseña
1. Usuario ingresa email
2. Se genera y envía código OTP
3. Usuario ingresa OTP
4. Usuario ingresa nueva contraseña
5. Contraseña actualizada

## 🧩 Componentes Principales

### Componentes Comunes
- `Button`: Botón reutilizable con variantes
- `Input`: Input de texto con validación
- `Card`: Contenedor con estilo
- `Alert`: Mensajes de alerta
- `Loading`: Indicador de carga

### Componentes de Autenticación
- `OTPInput`: Input para código OTP de 6 dígitos
- `PasswordInput`: Input de contraseña con indicador de fortaleza
- `EmailInput`: Input de email con validación

## 🔧 Configuración

### Variables de Entorno

Crear archivo `.env`:
```
VITE_API_BASE_URL=http://localhost:8081
```

## 📝 Scripts Disponibles

- `npm run dev`: Inicia servidor de desarrollo
- `npm run build`: Construye para producción
- `npm run preview`: Previsualiza build de producción
- `npm run lint`: Ejecuta linter

## 🔗 Integración con Backend

El frontend se comunica exclusivamente con el servicio Java en `http://localhost:8081`. El servicio Java se encarga de comunicarse internamente con el servicio Python cuando es necesario.

### Endpoints Utilizados

- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Login
- `POST /api/auth/otp` - Generar OTP
- `POST /api/auth/verify` - Verificar OTP
- `GET /api/auth/confirm-email` - Verificar estado de email

## 📚 Tecnologías

- React 18
- TypeScript
- Vite
- React Router
- Axios
- CSS Modules

## 🐛 Solución de Problemas

### Error de conexión con el backend
- Verificar que el servicio Java esté corriendo en el puerto 8081
- Verificar la URL en `src/utils/constants.ts`

### Error al generar OTP
- Verificar que el servicio Python esté corriendo en el puerto 8082
- Verificar logs del servicio Java

## 📄 Licencia

Este proyecto es parte de GOAT, una réplica para la universidad.

