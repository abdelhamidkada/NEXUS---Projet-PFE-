import React from 'react';
import { useTranslation } from 'react-i18next';

/**
 * Subcomponent to render the notification count badge.
 */
export const Badge = ({ count }) => {
  const { t } = useTranslation();
  return (
    <span className="text-[10px] bg-red-50 text-red-600 font-bold px-2 py-0.5 rounded-full">
      {t('header.new_notifications', { count, defaultValue: `${count} nouvelles` })}
    </span>
  );
};

/**
 * Dropdown menu for system notifications.
 */
const NotificationDropdown = ({ notifications, count, onMarkAsRead }) => {
  const { t } = useTranslation();

  return (
    <div className="absolute right-0 mt-2 w-80 bg-white border border-gray-200 rounded-2xl shadow-xl py-3 z-30 animate-in fade-in slide-in-from-top-2 duration-150">
      <div className="px-4 pb-2 border-b border-gray-100 flex items-center justify-between">
        <span className="text-xs font-bold text-gray-800">{t('header.notifications', 'Notifications')}</span>
        {notifications.length > 0 && <Badge count={notifications.length} />}
      </div>
      <div className="max-h-60 overflow-y-auto mt-2">
        {notifications.length === 0 ? (
          <p className="text-[11px] text-gray-400 text-center py-6 italic">
            {t('header.no_notifications', 'Aucune nouvelle notification.')}
          </p>
        ) : (
          notifications.map((notif) => (
            <div
              key={notif.id}
              className="px-4 py-2.5 hover:bg-gray-50 flex items-start justify-between gap-3 transition-colors border-b border-gray-50 last:border-0"
            >
              <div className="min-w-0 flex-1">
                <p className="text-[11px] text-gray-700 leading-tight font-medium">{notif.message}</p>
                <p className="text-[9px] text-gray-400 mt-1">
                  {new Date(notif.createdAt).toLocaleDateString('fr-FR', {
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </p>
              </div>
              <button
                onClick={() => onMarkAsRead(notif.id)}
                className="text-[9px] text-blue-600 hover:text-blue-800 hover:underline font-bold shrink-0 self-center"
              >
                {t('header.mark_read', 'Marquer lu')}
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default NotificationDropdown;
