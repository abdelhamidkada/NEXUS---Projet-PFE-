import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

/**
 * Zustand store to manage and persist global application settings.
 * Persists theme ('light' | 'dark'), language ('fr' | 'en'), and notification preferences.
 */
const useAppStore = create(
  persist(
    (set) => ({
      // --- State ---
      settings: {
        language: 'fr',
        theme: 'light',
        notificationsEnabled: true,
      },

      // --- Actions ---
      
      /**
       * Updates the application settings.
       * 
       * @param {Object} newSettings - Partial settings object to merge.
       */
      updateSettings: (newSettings) => set((state) => ({
        settings: { ...state.settings, ...newSettings }
      })),
    }),
    {
      name: 'nexus-app-storage', // Key used inside localstorage
      storage: createJSONStorage(() => localStorage), // Explicitly utilize standard localStorage wrapper
    }
  )
);

export default useAppStore;
