import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Translation dictionaries
const resources = {
  fr: {
    translation: {
      menu: {
        title: "Menu principal",
        dashboard: "Tableau de bord",
        profiles: "Profils employés",
        attendance: "Temps & Présences",
        leave_validation: "Validation RH",
        leave_requests: "Mes Congés",
        documents: "Documents RH",
        skills: "Compétences",
        settings: "Paramètres"
      },
      header: {
        search_placeholder: "Rechercher un profil, département…",
        no_results: "Aucun résultat trouvé",
        notifications: "Notifications",
        new_notifications_one: "1 nouvelle",
        new_notifications_other: "{{count}} nouvelles",
        no_notifications: "Aucune nouvelle notification.",
        mark_read: "Marquer lu",
        admin_role: "Administrateur RH",
        employee_role: "Employé",
        my_profile: "Mon Profil",
        my_clock: "Ma Pointeuse",
        logout: "Se déconnecter"
      },
      settings: {
        title: "Paramètres du système",
        subtitle: "Gérez vos préférences de langue, de thème et de notifications.",
        language: "Langue de l'interface",
        language_help: "Choisissez la langue d'affichage globale.",
        theme: "Thème visuel",
        theme_help: "Basculez entre le mode clair et le mode sombre.",
        notifications: "Notifications système",
        notifications_help: "Activer ou désactiver les alertes en temps réel.",
        save_success: "Paramètres sauvegardés localement.",
        french: "Français",
        english: "Anglais",
        dark_mode: "Mode Sombre",
        light_mode: "Mode Clair",
        enable_alerts: "Activer les alertes"
      }
    }
  },
  en: {
    translation: {
      menu: {
        title: "Main Menu",
        dashboard: "Dashboard",
        profiles: "Employee Profiles",
        attendance: "Time & Attendance",
        leave_validation: "HR Validation",
        leave_requests: "My Leaves",
        documents: "HR Documents",
        skills: "Skills Matrix",
        settings: "Settings"
      },
      header: {
        search_placeholder: "Search profile, department...",
        no_results: "No results found",
        notifications: "Notifications",
        new_notifications_one: "1 new",
        new_notifications_other: "{{count}} new",
        no_notifications: "No new notifications.",
        mark_read: "Mark read",
        admin_role: "HR Administrator",
        employee_role: "Employee",
        my_profile: "My Profile",
        my_clock: "My Clock",
        logout: "Sign Out"
      },
      settings: {
        title: "System Settings",
        subtitle: "Manage your language, theme, and notification preferences.",
        language: "Interface Language",
        language_help: "Choose the global display language.",
        theme: "Visual Theme",
        theme_help: "Switch between light and dark mode.",
        notifications: "System Notifications",
        notifications_help: "Enable or disable real-time alerts.",
        save_success: "Settings saved locally.",
        french: "French",
        english: "English",
        dark_mode: "Dark Mode",
        light_mode: "Light Mode",
        enable_alerts: "Enable alerts"
      }
    }
  }
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'fr',
    debug: false,
    interpolation: {
      escapeValue: false, // react already escapes values
    }
  });

export default i18n;
