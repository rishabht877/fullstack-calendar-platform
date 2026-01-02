/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                // Apple-style colors from CalendarGui.java
                'apple-bg': '#F8F8F8',       // Light Gray Background
                'apple-header': '#FFFFFF',   // White Header/Card
                'apple-blue': '#007AFF',     // Accent Blue
                'apple-text': '#1D1D1F',     // Primary Text
                'apple-gray': '#8E8E93',     // Secondary Text
                'apple-orange': '#FF9500',   // Today Highlight
                'apple-green': '#34C759',    // Event Dot
                'apple-border': '#E5E5EA',   // Border Color

                // Keep these for existing components until refactored
                primary: '#007AFF',
                secondary: '#8E8E93',
                success: '#34C759',
                danger: '#FF3B30',
                warning: '#FF9500',
            }
        },
    },
    plugins: [],
}
