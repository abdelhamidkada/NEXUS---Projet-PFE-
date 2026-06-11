import apiClient from './apiClient';
import useAuthStore from '../store/useAuthStore';

/**
 * Service API pour la gestion du temps et des présences (Time & Attendance).
 */
const timeTrackingApi = {
  /**
   * Enregistre un pointage (entrée ou sortie) pour l'employé connecté.
   *
   * @param {string} type - Le type de pointage ('CHECK_IN' ou 'CHECK_OUT').
   * @param {number|null} latitude - La latitude géographique de l'appareil.
   * @param {number|null} longitude - La longitude géographique de l'appareil.
   * @param {number|null} [employeeId=null] - Optionnel. ID de l'employé (déduit automatiquement si non fourni).
   * @returns {Promise<Object>} Le pointage enregistré renvoyé par le backend.
   */
  punch: async (type, latitude, longitude, employeeId = null) => {
    let resolvedEmployeeId = employeeId;

    // Si l'ID de l'employé n'est pas fourni, on le déduit depuis l'état d'authentification Zustand
    if (!resolvedEmployeeId) {
      const user = useAuthStore.getState().user;
      if (user) {
        resolvedEmployeeId = user.profileId || user.id;
      }
      if (!resolvedEmployeeId) {
        throw new Error("Impossible de pointer : aucun utilisateur connecté ou profil non résolu.");
      }
    }

    const response = await apiClient.post('/api/v1/tracking', {
      type,
      latitude,
      longitude
    }, {
      params: { employeeId: resolvedEmployeeId }
    });

    return response.data;
  },

  /**
   * Récupère le rapport journalier de temps d'un employé pour une date donnée.
   *
   * @param {number} employeeId - L'ID du profil de l'employé.
   * @param {string} date - La date ciblée au format ISO (YYYY-MM-DD).
   * @returns {Promise<Object>} Le rapport journalier contenant les heures travaillées et sup.
   */
  getDailyReport: async (employeeId, date) => {
    const response = await apiClient.get(`/api/v1/tracking/report/${employeeId}`, {
      params: { date }
    });
    return response.data;
  }
};

export default timeTrackingApi;
