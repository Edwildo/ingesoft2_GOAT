/**
 * Tema global estilo periódico
 * Todas las constantes de diseño centralizadas para reutilización
 */

// ============================================
// COLORES - Paleta estilo periódico
// ============================================
export const colors = {
  // Grises principales
  black: '#000000',
  gray900: '#111827',
  gray800: '#1f2937',
  gray700: '#374151',
  gray600: '#4b5563',
  gray500: '#6b7280',
  gray400: '#9ca3af',
  gray300: '#d1d5db',
  gray200: '#e5e7eb',
  gray100: '#f3f4f6',
  white: '#ffffff',

  // Colores de acento
  primary: '#1f2937',
  primaryDark: '#111827',
  accent: '#3b82f6',
  
  // Estados
  success: '#22c55e',
  successLight: '#f0fdf4',
  successBorder: '#86efac',
  successText: '#166534',
  
  error: '#dc2626',
  errorLight: '#fef2f2',
  errorBorder: '#fca5a5',
  errorText: '#991b1b',
  
  warning: '#f59e0b',
  warningLight: '#fffbeb',
  warningBorder: '#fde047',
  warningText: '#854d0e',
  
  info: '#3b82f6',
  infoLight: '#eff6ff',
  infoBorder: '#93c5fd',
  infoText: '#1e40af',
} as const;

// ============================================
// TIPOGRAFÍA - Fuentes estilo periódico
// ============================================
export const typography = {
  // Familias de fuentes
  fontSerif: "'Georgia', 'Times New Roman', serif",
  fontSans: "-apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif",
  fontMono: "'Courier New', Courier, monospace",

  // Tamaños de fuente
  fontSize: {
    xs: '0.75rem',    // 12px
    sm: '0.875rem',   // 14px
    base: '1rem',     // 16px
    lg: '1.125rem',   // 18px
    xl: '1.25rem',    // 20px
    '2xl': '1.5rem',  // 24px
    '3xl': '2rem',    // 32px
    '4xl': '2.5rem',  // 40px
  },

  // Pesos de fuente
  fontWeight: {
    normal: 400,
    medium: 500,
    semibold: 600,
    bold: 700,
  },

  // Altura de línea
  lineHeight: {
    tight: 1.2,
    normal: 1.5,
    relaxed: 1.6,
    loose: 2,
  },

  // Espaciado de letras
  letterSpacing: {
    tighter: '-0.02em',
    tight: '-0.01em',
    normal: '0',
    wide: '0.05em',
    wider: '0.1em',
  },
} as const;

// ============================================
// ESPACIADO - Sistema de espaciado consistente
// ============================================
export const spacing = {
  xs: '0.25rem',   // 4px
  sm: '0.5rem',    // 8px
  md: '1rem',      // 16px
  lg: '1.5rem',    // 24px
  xl: '2rem',      // 32px
  '2xl': '3rem',   // 48px
  '3xl': '4rem',   // 64px
} as const;

// ============================================
// BORDES - Estilo periódico
// ============================================
export const borders = {
  // Radios
  radius: {
    none: '0',
    sm: '0.25rem',   // 4px
    md: '0.5rem',    // 8px
    lg: '0.75rem',  // 12px
    full: '9999px',
  },

  // Anchos
  width: {
    none: '0',
    thin: '1px',
    base: '2px',
    thick: '3px',
  },

  // Estilos
  style: {
    solid: 'solid',
    dashed: 'dashed',
    dotted: 'dotted',
  },
} as const;

// ============================================
// SOMBRAS - Sistema de sombras
// ============================================
export const shadows = {
  none: 'none',
  sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
  md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
  lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
  xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
  inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
} as const;

// ============================================
// TRANSICIONES - Animaciones suaves
// ============================================
export const transitions = {
  fast: '0.15s ease',
  base: '0.2s ease',
  slow: '0.3s ease',
  slower: '0.5s ease',
} as const;

// ============================================
// BREAKPOINTS - Responsive design
// ============================================
export const breakpoints = {
  sm: '640px',
  md: '768px',
  lg: '1024px',
  xl: '1280px',
  '2xl': '1536px',
} as const;

// ============================================
// Z-INDEX - Capas de la aplicación
// ============================================
export const zIndex = {
  base: 0,
  dropdown: 1000,
  sticky: 1020,
  fixed: 1030,
  modalBackdrop: 1040,
  modal: 1050,
  popover: 1060,
  tooltip: 1070,
  loading: 9999,
} as const;

// ============================================
// EXPORTACIÓN DE TEMA COMPLETO
// ============================================
export const theme = {
  colors,
  typography,
  spacing,
  borders,
  shadows,
  transitions,
  breakpoints,
  zIndex,
} as const;

export type Theme = typeof theme;

