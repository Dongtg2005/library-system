module.exports = {
  darkMode: 'class',
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#4f46e5',
        accent: '#facc15',
        neutral: '#f3f4f6',
      },
      boxShadow: {
        glow: '0 20px 60px rgba(79, 70, 229, 0.15)',
      },
      backgroundImage: {
        'dashboard-gradient': 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 45%, #06b6d4 100%)',
        'card-gradient': 'linear-gradient(135deg, rgba(79,70,229,0.12), rgba(250,204,21,0.12))',
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
