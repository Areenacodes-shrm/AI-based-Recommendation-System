/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        navy: {
          950: '#060a12',
          900: '#0c1424',
          850: '#111c33',
          800: '#16233f',
          700: '#223456',
          600: '#334b77'
        },
        brand: {
          violet: '#8b5cf6',
          violetLight: '#a78bfa',
          teal: '#14b8a6',
          tealLight: '#2dd4bf',
          amber: '#f59e0b',
          rose: '#f43f5e'
        }
      }
    },
  },
  plugins: [],
}
