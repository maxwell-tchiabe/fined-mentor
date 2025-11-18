/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        // Brand colors
        'brand': {
          50: '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6', // Primary brand color
          600: '#0d9488',
          700: '#0f766e', // Secondary brand color
          800: '#115e59',
          900: '#134e4a',
          950: '#042f2e',
        },
        
        // Base colors (dark theme)
        'base': {
          100: '#0f172a', // slate-900
          200: '#1e293b', // slate-800
          300: '#334155', // slate-700
          400: '#475569', // slate-600
          500: '#64748b', // slate-500
        },
        
        // Content colors
        'content': {
          100: '#f8fafc', // slate-50
          200: '#e2e8f0', // slate-200
          300: '#cbd5e1', // slate-300
          400: '#94a3b8', // slate-400
        },
        
        // Semantic colors
        'success': {
          50: '#f0fdf4',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
        },
        
        'warning': {
          50: '#fffbeb',
          500: '#f59e0b',
          600: '#d97706',
          700: '#b45309',
        },
        
        'error': {
          50: '#fef2f2',
          500: '#ef4444',
          600: '#dc2626',
          700: '#b91c1c',
        },
        
        'info': {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        }
      },
      
      // Custom animations
      animation: {
        'pulse-fast': 'pulse 1s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.3s ease-out',
        'slide-in-right': 'slideInRight 0.3s ease-out',
        'slide-in-left': 'slideInLeft 0.3s ease-out',
        'bounce-subtle': 'bounceSubtle 2s infinite',
        'spin-slow': 'spin 3s linear infinite',
      },
      
      // Keyframes for custom animations
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        slideInRight: {
          '0%': { opacity: '0', transform: 'translateX(20px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        slideInLeft: {
          '0%': { opacity: '0', transform: 'translateX(-20px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        bounceSubtle: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-5px)' },
        },
      },
      
      // Custom spacing scale
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
        '128': '32rem',
      },
      
      // Custom typography
      fontSize: {
        'xxs': ['0.625rem', '0.75rem'], // 10px
        'xs': ['0.75rem', '1rem'],      // 12px
        'sm': ['0.875rem', '1.25rem'],  // 14px
        'base': ['1rem', '1.5rem'],     // 16px
        'lg': ['1.125rem', '1.75rem'],  // 18px
        'xl': ['1.25rem', '1.75rem'],   // 20px
        '2xl': ['1.5rem', '2rem'],      // 24px
        '3xl': ['1.875rem', '2.25rem'], // 30px
        '4xl': ['2.25rem', '2.5rem'],   // 36px
        '5xl': ['3rem', '1'],           // 48px
        '6xl': ['3.75rem', '1'],        // 60px
      },
      
      // Custom box shadow
      boxShadow: {
        'soft': '0 2px 15px -3px rgba(0, 0, 0, 0.1), 0 10px 20px -2px rgba(0, 0, 0, 0.04)',
        'medium': '0 4px 25px -5px rgba(0, 0, 0, 0.15), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        'large': '0 10px 50px -12px rgba(0, 0, 0, 0.25)',
        'glow': '0 0 20px rgba(20, 184, 166, 0.3)',
        'glow-md': '0 0 30px rgba(20, 184, 166, 0.4)',
      },
      
      // Custom border radius
      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.5rem',
      },
      
      // Custom backdrop blur
      backdropBlur: {
        xs: '2px',
      },
      
      // Custom z-index
      zIndex: {
        '60': '60',
        '70': '70',
        '80': '80',
        '90': '90',
        '100': '100',
      },
      
      // Custom screens
      screens: {
        'xs': '475px',
        '3xl': '1600px',
      },
      
      // Custom font families
      fontFamily: {
        'sans': ['Inter', 'ui-sans-serif', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'Helvetica Neue', 'Arial', 'Noto Sans', 'sans-serif'],
        'mono': ['JetBrains Mono', 'ui-monospace', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', 'Liberation Mono', 'Courier New', 'monospace'],
      },
    },
  },
  
  // Variants
  variants: {
    extend: {
      opacity: ['disabled'],
      cursor: ['disabled'],
      backgroundColor: ['active', 'disabled'],
      textColor: ['disabled'],
      borderColor: ['disabled'],
      scale: ['active'],
      animation: ['hover', 'focus'],
    },
  },
  
  // Plugins
  plugins: [
    require('@tailwindcss/forms')({
      strategy: 'class',
    }),
    require('@tailwindcss/typography')({
      className: 'prose',
      modifiers: ['sm', 'lg'],
    }),
  ],
}