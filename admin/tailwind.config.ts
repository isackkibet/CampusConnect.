import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./lib/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          950: "#0b1220",
          900: "#0f1b2d",
          800: "#15243a",
          700: "#1d3350",
        },
        teal: {
          500: "#0f9b8e",
          600: "#0d8479",
        },
      },
    },
  },
  plugins: [],
};

export default config;
