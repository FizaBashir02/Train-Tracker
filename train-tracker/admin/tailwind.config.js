/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: "#0F7A3E",
        secondary: "#2E9E5B",
        darkBg: "#121212",
        darkSurface: "#1E1E1E"
      }
    },
  },
  plugins: [],
}
