import { ref, watch } from 'vue'

export type Theme = 'light' | 'dark' | 'auto'

const THEME_KEY = 'app-theme-preference'

// Global state
const theme = ref<Theme>('auto')
const systemDark = ref(false)

// Initialize system preference listener
const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
systemDark.value = mediaQuery.matches

mediaQuery.addEventListener('change', (e) => {
  systemDark.value = e.matches
  if (theme.value === 'auto') {
    applyTheme()
  }
})

// Apply theme to DOM
const applyTheme = () => {
  const isDark =
    theme.value === 'dark' || (theme.value === 'auto' && systemDark.value)

  if (isDark) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// Initialize from storage
const initTheme = () => {
  const savedTheme = localStorage.getItem(THEME_KEY) as Theme
  if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
    theme.value = savedTheme
  }
  applyTheme()
}

// Watch for changes
watch(theme, (newTheme) => {
  localStorage.setItem(THEME_KEY, newTheme)
  applyTheme()
})

export const useTheme = () => {
  return {
    theme,
    initTheme,
  }
}
