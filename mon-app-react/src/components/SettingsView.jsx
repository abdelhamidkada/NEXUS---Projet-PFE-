import React from 'react';
import { useTranslation } from 'react-i18next';
import useAppStore from '../store/useAppStore';
import { 
  Globe, 
  Moon, 
  Sun, 
  Bell, 
  Settings, 
  HelpCircle,
  CheckCircle
} from 'lucide-react';

export default function SettingsView({ onShowToast }) {
  const { t } = useTranslation();
  const { settings, updateSettings } = useAppStore();

  const handleLanguageChange = (e) => {
    const lang = e.target.value;
    updateSettings({ language: lang });
    if (onShowToast) {
      onShowToast(t('settings.save_success', 'Paramètres sauvegardés localement.'), 'success');
    }
  };

  const toggleTheme = () => {
    const newTheme = settings.theme === 'dark' ? 'light' : 'dark';
    updateSettings({ theme: newTheme });
    if (onShowToast) {
      onShowToast(t('settings.save_success', 'Paramètres sauvegardés localement.'), 'success');
    }
  };

  const toggleNotifications = () => {
    const newVal = !settings.notificationsEnabled;
    updateSettings({ notificationsEnabled: newVal });
    if (onShowToast) {
      onShowToast(t('settings.save_success', 'Paramètres sauvegardés localement.'), 'success');
    }
  };

  return (
    <div className="max-w-3xl mx-auto py-6 animate-in fade-in duration-300">
      {/* Title section */}
      <div className="mb-8">
        <h1 className="text-xl md:text-2xl font-black tracking-tight text-gray-900 dark:text-white flex items-center gap-3">
          <Settings className="h-6 w-6 text-blue-600 dark:text-blue-400 animate-spin-slow" />
          {t('settings.title', 'Paramètres du système')}
        </h1>
        <p className="text-xs text-gray-500 dark:text-slate-400 mt-1.5">
          {t('settings.subtitle', 'Gerez vos preferences de langue, de theme et de notifications.')}
        </p>
      </div>

      {/* Grid Settings */}
      <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-md border border-gray-200 dark:border-slate-800 rounded-3xl shadow-xl overflow-hidden p-6 md:p-8 space-y-8">
        
        {/* 1. Interface Language */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-gray-100 dark:border-slate-800">
          <div className="flex items-start gap-4">
            <div className="p-3 bg-blue-50 dark:bg-blue-950/40 text-blue-600 dark:text-blue-400 rounded-2xl shrink-0">
              <Globe className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-800 dark:text-slate-100">{t('settings.language', "Langue de l'interface")}</h3>
              <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">{t('settings.language_help', "Choisissez la langue d'affichage globale.")}</p>
            </div>
          </div>
          <div className="w-full md:w-48">
            <select
              value={settings.language || 'fr'}
              onChange={handleLanguageChange}
              className="w-full px-4 py-2.5 text-xs font-semibold bg-gray-50 dark:bg-slate-800 text-gray-800 dark:text-slate-100 border border-gray-200 dark:border-slate-700 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent cursor-pointer transition-all"
            >
              <option value="fr">{t('settings.french', 'Français')}</option>
              <option value="en">{t('settings.english', 'English')}</option>
            </select>
          </div>
        </div>

        {/* 2. Visual Theme */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-gray-100 dark:border-slate-800">
          <div className="flex items-start gap-4">
            <div className="p-3 bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 rounded-2xl shrink-0">
              {settings.theme === 'dark' ? <Moon className="h-5 w-5" /> : <Sun className="h-5 w-5" />}
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-800 dark:text-slate-100">{t('settings.theme', 'Theme visuel')}</h3>
              <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">{t('settings.theme_help', 'Basculez entre le mode clair et le mode sombre.')}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">
              {settings.theme === 'dark' ? t('settings.dark_mode', 'Mode Sombre') : t('settings.light_mode', 'Mode Clair')}
            </span>
            <button
              onClick={toggleTheme}
              className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none
                ${settings.theme === 'dark' ? 'bg-blue-600' : 'bg-gray-200 dark:bg-slate-800'}`}
            >
              <span
                className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out
                  ${settings.theme === 'dark' ? 'translate-x-5' : 'translate-x-0'}`}
              />
            </button>
          </div>
        </div>

        {/* 3. System Notifications */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="flex items-start gap-4">
            <div className="p-3 bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 rounded-2xl shrink-0">
              <Bell className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-gray-800 dark:text-slate-100">{t('settings.notifications', 'Notifications systeme')}</h3>
              <p className="text-xs text-gray-500 dark:text-slate-400 mt-1">{t('settings.notifications_help', 'Activer ou desactiver les alertes en temps reel.')}</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold text-gray-500 dark:text-slate-400">
              {t('settings.enable_alerts', 'Activer les alertes')}
            </span>
            <button
              onClick={toggleNotifications}
              className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none
                ${settings.notificationsEnabled ? 'bg-blue-600' : 'bg-gray-200 dark:bg-slate-800'}`}
            >
              <span
                className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out
                  ${settings.notificationsEnabled ? 'translate-x-5' : 'translate-x-0'}`}
              />
            </button>
          </div>
        </div>

      </div>

      {/* Info card footer */}
      <div className="mt-6 p-4 bg-blue-50/50 dark:bg-slate-900/40 border border-blue-100/30 dark:border-slate-800 rounded-2xl flex items-start gap-3.5 text-xs text-blue-700 dark:text-blue-300">
        <HelpCircle className="h-4 w-4 shrink-0 text-blue-500 mt-0.5" />
        <p className="leading-relaxed">
          Les préférences sont enregistrées dans le cache local de votre navigateur de manière sécurisée et persisteront après réactualisation de la page.
        </p>
      </div>
    </div>
  );
}
