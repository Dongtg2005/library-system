module.exports = {
  darkMode: 'class',
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#14b8a6',
        accent: '#06b6d4',
        neutral: '#f3f4f6',
      },
      boxShadow: {
        glow: '0 20px 60px rgba(20, 184, 166, 0.15)',
      },
      backgroundImage: {
        'dashboard-gradient': 'linear-gradient(135deg, #0f766e 0%, #14b8a6 45%, #06b6d4 100%)',
        'card-gradient': 'linear-gradient(135deg, rgba(20,184,166,0.12), rgba(6,182,212,0.12))',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-8px)' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
      animation: {
        float: 'float 6s ease-in-out infinite',
        shimmer: 'shimmer 2s linear infinite',
      },
    },
  },
  plugins: [],
};
