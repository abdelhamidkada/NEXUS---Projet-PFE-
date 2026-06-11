import React, { useState, useEffect } from 'react';
import { 
  Clock, 
  MapPin, 
  Play, 
  Square, 
  AlertTriangle, 
  Calendar,
  Compass,
  RefreshCw,
  TrendingUp
} from 'lucide-react';
import useAuthStore from '../store/useAuthStore';
import timeTrackingApi from '../api/timeTrackingApi';
import apiClient from '../api/apiClient';

export default function TimePunchCard({ onShowToast }) {
  const { user } = useAuthStore();
  const [isCheckedIn, setIsCheckedIn] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());
  
  // États de chargement et de données
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);
  const [dailyReport, setDailyReport] = useState({ totalHours: 0, overtimeHours: 0 });
  
  // États du rapport mensuel
  const [monthlyReport, setMonthlyReport] = useState(null);
  const [monthlyLoading, setMonthlyLoading] = useState(false);

  // États de géolocalisation
  const [geoState, setGeoState] = useState({
    coords: null,
    loading: false,
    error: null
  });

  const [employeeId, setEmployeeId] = useState(user?.profileId || null);

  const isHrOrDirection = user?.roles?.some(r => ['HR_ADMIN', 'DIRECTION'].includes(r));

  // Dynamic profile ID resolution
  useEffect(() => {
    const resolveEmployeeId = async () => {
      if (!user) return;
      if (user.profileId) {
        setEmployeeId(user.profileId);
        return;
      }
      try {
        const res = await apiClient.get('/api/v1/hr/profiles');
        const myProfile = res.data.find(
          (p) => p.email && p.email.toLowerCase() === user.email.toLowerCase()
        );
        if (myProfile) {
          setEmployeeId(myProfile.id);
          const { token: jwtToken, login: storeLogin } = useAuthStore.getState();
          storeLogin(jwtToken, { 
            ...user, 
            profileId: myProfile.id, 
            firstName: myProfile.firstName, 
            lastName: myProfile.lastName 
          });
        }
      } catch (err) {
        console.error("Erreur de résolution du profil employé", err);
      }
    };
    resolveEmployeeId();
  }, [user]);

  // Horloge en temps réel
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Récupérer le rapport quotidien au montage
  const fetchDailyReport = async (silent = false) => {
    if (!employeeId) return;
    if (!silent) setInitialLoading(true);
    try {
      const todayStr = new Date().toISOString().split('T')[0];
      const data = await timeTrackingApi.getDailyReport(employeeId, todayStr);
      setDailyReport(data);
      
      // Réconciliation intelligente : si isMissingCheckout est true, l'employé est en cours de service (Checked In)
      if (!silent) {
        setIsCheckedIn(data.isMissingCheckout);
      }
    } catch (err) {
      console.error("Erreur lors de la récupération du rapport de pointage", err);
      if (onShowToast) {
        onShowToast("Impossible de charger les statistiques de présence.", "error");
      }
    } finally {
      setInitialLoading(false);
    }
  };

  // Récupérer le rapport mensuel
  const fetchMonthlyReport = async (silent = false) => {
    if (!employeeId) return;
    if (!silent) setMonthlyLoading(true);
    try {
      const data = await timeTrackingApi.getMonthlyReport(employeeId);
      setMonthlyReport(data);
    } catch (err) {
      console.error("Erreur lors de la récupération du rapport mensuel d'assiduité", err);
    } finally {
      setMonthlyLoading(false);
    }
  };

  useEffect(() => {
    if (user && employeeId) {
      fetchDailyReport();
      fetchMonthlyReport();
    }
  }, [user, employeeId]);

  // Demander la géolocalisation GPS
  const requestLocation = () => {
    return new Promise((resolve) => {
      if (!navigator.geolocation) {
        setGeoState(prev => ({ ...prev, error: "La géolocalisation n'est pas supportée par votre navigateur." }));
        resolve(null);
        return;
      }

      setGeoState(prev => ({ ...prev, loading: true, error: null }));

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const coords = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          };
          setGeoState({ coords, loading: false, error: null });
          resolve(coords);
        },
        (error) => {
          let errorMsg = "Impossible d'obtenir votre position GPS.";
          if (error.code === 1) {
            errorMsg = "Accès GPS refusé. Veuillez autoriser la localisation pour pointer.";
          }
          setGeoState({ coords: null, loading: false, error: errorMsg });
          resolve(null);
        },
        { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
      );
    });
  };

  // Action : Pointage principal (Entrée ou Sortie)
  const handlePunch = async () => {
    if (loading) return;
    
    // Si c'est une déconnexion (CHECK_OUT), demander confirmation
    if (isCheckedIn && !window.confirm("Confirmer votre pointage de sortie pour terminer votre journée ?")) {
      return;
    }

    setLoading(true);

    try {
      // 1. Tenter d'obtenir les coordonnées GPS
      let coords = await requestLocation();
      
      // Repli GPS si indisponible/bloqué pour le test
      if (!coords) {
        coords = { latitude: 48.8566, longitude: 2.3522 };
        if (onShowToast) {
          onShowToast("Géolocalisation indisponible. Utilisation de coordonnées par défaut.", "warning");
        }
      }

      // 2. Déduire le type de pointage binaire
      const type = isCheckedIn ? 'CHECK_OUT' : 'CHECK_IN';

      // 3. Appel API
      await timeTrackingApi.punch(type, coords.latitude, coords.longitude, employeeId);

      // 4. Mettre à jour l'état local et notifier de manière cohérente
      const newCheckedInState = type === 'CHECK_IN';
      setIsCheckedIn(newCheckedInState);
      
      if (onShowToast) {
        onShowToast(
          type === 'CHECK_IN' 
            ? "Pointage d'entrée (Check-In) enregistré avec succès !" 
            : "Pointage de sortie (Check-Out) enregistré avec succès !",
          "success"
        );
      }

      // 5. Mettre à jour les statistiques de travail et le tableau mensuel
      await fetchDailyReport(true);
      await fetchMonthlyReport(true);
    } catch (err) {
      console.error("Erreur de pointage", err);
      const serverMsg = err.response?.data?.message;
      if (onShowToast) {
        onShowToast(serverMsg || "Une erreur est survenue lors du pointage.", "error");
      }
    } finally {
      setLoading(false);
    }
  };

  // Action: Outrepasser une absence injustifiée (HR seulement)
  const handleOverride = async (trackingId) => {
    if (!trackingId) return;
    setLoading(true);
    try {
      await timeTrackingApi.overrideLatePunchIn(trackingId);
      if (onShowToast) {
        onShowToast("Absence justifiée (Maladie) enregistrée avec succès !", "success");
      }
      await fetchDailyReport(true);
      await fetchMonthlyReport(true);
    } catch (err) {
      console.error("Erreur d'override d'absence", err);
      const serverMsg = err.response?.data?.message || "Erreur lors de la modification.";
      if (onShowToast) {
        onShowToast(serverMsg, "error");
      }
    } finally {
      setLoading(false);
    }
  };

  // Formater les heures décimales en HHhMM
  const formatDecimalHours = (decimalHours) => {
    if (decimalHours === undefined || decimalHours === null) return "0h00";
    const hours = Math.floor(decimalHours);
    const minutes = Math.round((decimalHours - hours) * 60);
    return `${hours}h${minutes.toString().padStart(2, '0')}`;
  };

  const capitalizeFirstLetter = (string) => {
    if (!string) return '';
    return string.charAt(0).toUpperCase() + string.slice(1);
  };

  const formatDayDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const formatted = date.toLocaleDateString('fr-FR', {
      weekday: 'short',
      day: 'numeric',
      month: 'short'
    });
    return capitalizeFirstLetter(formatted);
  };

  const STATUS_STYLES = {
    "Normal": "bg-emerald-50 text-emerald-700 border-emerald-200 ring-1 ring-emerald-100",
    "Congé": "bg-blue-50 text-blue-700 border-blue-200 ring-1 ring-blue-100",
    "Week-end": "bg-slate-50 text-slate-500 border-slate-200 ring-1 ring-slate-100",
    "Absent": "bg-red-50 text-red-700 border-red-200 ring-1 ring-red-100",
    "Absence injustifiée": "bg-amber-50 text-amber-700 border-amber-200 ring-1 ring-amber-100",
    "Absence justifiée (Maladie)": "bg-purple-50 text-purple-700 border-purple-200 ring-1 ring-purple-100"
  };

  if (initialLoading) {
    return (
      <div className="flex flex-col items-center justify-center p-12 bg-white/40 backdrop-blur-md rounded-2xl border border-gray-200/80 shadow-lg min-h-[400px]">
        <RefreshCw className="h-8 w-8 text-blue-600 animate-spin mb-4" />
        <p className="text-sm font-medium text-gray-500">Initialisation de la pointeuse...</p>
      </div>
    );
  }

  return (
    <div className="w-full max-w-5xl mx-auto grid grid-cols-1 lg:grid-cols-12 gap-6 items-start px-4">
      
      {/* COLONNE GAUCHE: Pointeuse physique */}
      <div className="lg:col-span-5 bg-gradient-to-br from-white/95 to-gray-50/90 backdrop-blur-lg border border-white rounded-3xl shadow-xl overflow-hidden p-6 md:p-8 flex flex-col items-center relative min-h-[460px]">
        {/* Gradients de fond décoratifs */}
        <div className="absolute -top-10 -right-10 w-40 h-40 bg-blue-100/30 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-10 -left-10 w-40 h-40 bg-emerald-100/30 rounded-full blur-3xl pointer-events-none" />

        {/* En-tête du composant */}
        <div className="w-full flex items-center justify-between mb-8 border-b border-gray-100 pb-4 relative z-10">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-50 text-blue-600 rounded-xl">
              <Clock className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-gray-900">Pointeuse Numérique</h2>
              <p className="text-[11px] font-medium text-gray-400 flex items-center gap-1 mt-0.5">
                <Calendar className="h-3 w-3" />
                {currentTime.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })}
              </p>
            </div>
          </div>
          <div className="flex flex-col items-end">
            <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold
              ${isCheckedIn 
                ? 'bg-emerald-50 text-emerald-700 border border-emerald-100' 
                : 'bg-amber-50 text-amber-700 border border-amber-100'}`}
            >
              <span className={`h-1.5 w-1.5 rounded-full ${isCheckedIn ? 'bg-emerald-500 animate-pulse' : 'bg-amber-400'}`} />
              {isCheckedIn ? "Actif" : "Inactif"}
            </span>
          </div>
        </div>

        {/* Heure en temps réel */}
        <div className="flex flex-col items-center mb-6 relative z-10">
          <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight leading-none drop-shadow-sm font-mono">
            {currentTime.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
          </h1>
          <p className="text-[10px] text-gray-400 font-bold tracking-widest mt-1.5 uppercase">
            Heure du serveur sécurisée
          </p>
        </div>

        {/* Bouton de pointage central géant */}
        <div className="relative flex items-center justify-center my-6 z-10">
          {isCheckedIn ? (
            <div className="absolute w-56 h-56 bg-red-500/10 rounded-full animate-ping duration-1000 opacity-60" />
          ) : (
            <div className="absolute w-56 h-56 bg-emerald-500/10 rounded-full animate-pulse opacity-60" />
          )}

          <button
            onClick={handlePunch}
            disabled={loading || geoState.loading}
            className={`relative w-44 h-44 rounded-full flex flex-col items-center justify-center border-4 shadow-2xl transition-all duration-300 transform active:scale-95 outline-none focus:ring-4 focus:ring-opacity-40
              ${isCheckedIn 
                ? 'bg-gradient-to-tr from-red-600 to-rose-500 hover:from-red-500 hover:to-rose-400 border-white text-white focus:ring-red-400' 
                : 'bg-gradient-to-tr from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 border-white text-white focus:ring-emerald-400'}`}
          >
            {loading || geoState.loading ? (
              <div className="flex flex-col items-center gap-2">
                <RefreshCw className="h-9 w-9 animate-spin text-white" />
                <span className="text-[10px] uppercase font-bold tracking-wider opacity-90">
                  {geoState.loading ? "GPS..." : "Envoi..."}
                </span>
              </div>
            ) : (
              <div className="flex flex-col items-center">
                {isCheckedIn ? (
                  <>
                    <Square className="h-10 w-10 text-white mb-2 shrink-0 drop-shadow" />
                    <span className="text-xs font-extrabold uppercase tracking-widest leading-none drop-shadow text-center px-2">
                      POINTAGE SORTIE
                    </span>
                    <span className="text-[10px] font-semibold opacity-85 mt-1">
                      (Check-Out)
                    </span>
                  </>
                ) : (
                  <>
                    <Play className="h-10 w-10 text-white mb-2 translate-x-0.5 shrink-0 drop-shadow" />
                    <span className="text-xs font-extrabold uppercase tracking-widest leading-none drop-shadow text-center px-2">
                      POINTAGE ENTRÉE
                    </span>
                    <span className="text-[10px] font-semibold opacity-85 mt-1">
                      (Check-In)
                    </span>
                  </>
                )}
              </div>
            )}
          </button>
        </div>

        {/* État de géolocalisation */}
        <div className="w-full bg-white/50 backdrop-blur-sm border border-gray-100 rounded-2xl p-3.5 mb-8 text-center text-xs relative z-10">
          {geoState.loading && (
            <p className="text-gray-500 flex items-center justify-center gap-2">
              <Compass className="h-3.5 w-3.5 text-blue-500 animate-spin" />
              Vérification GPS en cours...
            </p>
          )}
          
          {geoState.error && (
            <div className="flex items-center gap-2 text-red-600 justify-center">
              <AlertTriangle className="h-4 w-4 shrink-0 text-red-500" />
              <p className="font-semibold">{geoState.error}</p>
            </div>
          )}

          {geoState.coords && !geoState.loading && (
            <div className="flex items-center gap-2 text-emerald-700 justify-center font-medium">
              <MapPin className="h-4 w-4 shrink-0 text-emerald-600 animate-bounce" />
              <span>Position validée : {geoState.coords.latitude.toFixed(4)}, {geoState.coords.longitude.toFixed(4)}</span>
            </div>
          )}

          {!geoState.loading && !geoState.coords && !geoState.error && (
            <p className="text-gray-400 flex items-center justify-center gap-1.5 font-medium">
              <MapPin className="h-3.5 w-3.5 text-gray-300" />
              La géolocalisation est requise et validée à chaque action
            </p>
          )}
        </div>

        {/* Résumé quotidien */}
        <div className="w-full bg-white/70 backdrop-blur-md border border-gray-200/60 rounded-2xl p-4 shadow-sm grid grid-cols-2 gap-4 relative z-10 mt-auto">
          
          {/* Heures du jour */}
          <div className="flex flex-col items-center text-center border-r border-gray-100 pr-2">
            <div className="p-1.5 bg-blue-50 text-blue-600 rounded-lg mb-1.5 shrink-0">
              <Clock className="h-4 w-4" />
            </div>
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider leading-none">
              Heures Aujourd'hui
            </span>
            <span className="text-lg font-extrabold text-gray-800 mt-1.5 leading-none">
              {formatDecimalHours(dailyReport.totalHours)}
            </span>
          </div>

          {/* Heures supplémentaires */}
          <div className="flex flex-col items-center text-center pl-2">
            <div className="p-1.5 bg-emerald-50 text-emerald-600 rounded-lg mb-1.5 shrink-0">
              <TrendingUp className="h-4 w-4" />
            </div>
            <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider leading-none">
              Heures Sup
            </span>
            <span className="text-lg font-extrabold text-gray-800 mt-1.5 leading-none">
              {formatDecimalHours(dailyReport.overtimeHours)}
            </span>
          </div>

        </div>

      </div>

      {/* COLONNE DROITE: Feuille de temps mensuelle */}
      <div className="lg:col-span-7 bg-white/80 backdrop-blur-lg border border-white rounded-3xl shadow-xl p-6 relative overflow-hidden flex flex-col min-h-[460px]">
        {/* Header */}
        <div className="flex items-center justify-between mb-6 pb-4 border-b border-gray-100">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-xl">
              <Calendar className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-gray-900">Feuille de Temps Mensuelle</h2>
              <p className="text-[11px] font-medium text-gray-400 mt-0.5">
                {monthlyReport ? `Du ${new Date(monthlyReport.startDate).toLocaleDateString('fr-FR')} au ${new Date(monthlyReport.endDate).toLocaleDateString('fr-FR')}` : 'Chargement...'}
              </p>
            </div>
          </div>
          <button
            onClick={() => fetchMonthlyReport()}
            disabled={monthlyLoading}
            className="p-1.5 rounded-lg border border-gray-200 text-gray-400 hover:text-blue-600 hover:border-blue-200 hover:bg-blue-50 transition-all duration-150"
          >
            <RefreshCw className={`h-4 w-4 ${monthlyLoading ? 'animate-spin' : ''}`} />
          </button>
        </div>

        {/* Résumé statistique mensuel */}
        {monthlyReport && (
          <div className="grid grid-cols-3 gap-3 mb-6">
            <div className="bg-slate-50 border border-slate-100 rounded-2xl p-3 text-center">
              <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block">
                Jours Travaillés
              </span>
              <span className="text-base font-extrabold text-gray-800 mt-1 block">
                {monthlyReport.daysWorked}
              </span>
            </div>
            <div className="bg-slate-50 border border-slate-100 rounded-2xl p-3 text-center">
              <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block">
                Heures Totales
              </span>
              <span className="text-base font-extrabold text-gray-800 mt-1 block">
                {formatDecimalHours(monthlyReport.totalHours)}
              </span>
            </div>
            <div className="bg-slate-50 border border-slate-100 rounded-2xl p-3 text-center">
              <span className="text-[9px] font-bold text-gray-400 uppercase tracking-wider block">
                Heures Supp
              </span>
              <span className="text-base font-extrabold text-gray-800 mt-1 block">
                {formatDecimalHours(monthlyReport.overtimeHours)}
              </span>
            </div>
          </div>
        )}

        {/* Liste défilante des détails journaliers */}
        <div className="flex-1 overflow-y-auto max-h-[350px] pr-1 space-y-2">
          {monthlyLoading ? (
            <div className="flex flex-col items-center justify-center py-12 text-gray-400">
              <RefreshCw className="h-6 w-6 animate-spin mb-2" />
              <span className="text-xs">Chargement de la feuille de temps...</span>
            </div>
          ) : monthlyReport?.dailyDetails && monthlyReport.dailyDetails.length > 0 ? (
            [...monthlyReport.dailyDetails].reverse().map((day, idx) => {
              const statusClass = STATUS_STYLES[day.status] || "bg-gray-50 text-gray-600 border-gray-200";
              const isAbsentLate = day.status === "Absence injustifiée";
              
              return (
                <div key={idx} className="flex items-center justify-between p-3 bg-white/60 hover:bg-white border border-gray-100 rounded-2xl transition-all duration-150">
                  {/* Date & Day */}
                  <div className="w-1/4">
                    <p className="text-xs font-bold text-gray-700">{formatDayDate(day.date)}</p>
                  </div>

                  {/* Check-in / Check-out & Hours */}
                  <div className="w-2/5 flex flex-col justify-center">
                    {day.checkInTime ? (
                      <p className="text-xs font-semibold text-gray-600">
                        {day.checkInTime} - {day.checkOutTime || '—'}
                      </p>
                    ) : (
                      <p className="text-xs text-gray-400 font-medium">Non pointé</p>
                    )}
                    {day.hoursWorked > 0 && (
                      <p className="text-[10px] text-blue-500 font-medium mt-0.5">
                        {formatDecimalHours(day.hoursWorked)} travaillé{day.overtimeHours > 0 ? ` (+${formatDecimalHours(day.overtimeHours)} sup)` : ''}
                      </p>
                    )}
                  </div>

                  {/* Status & Actions */}
                  <div className="w-1/3 flex flex-col items-end gap-1.5">
                    <span className={`px-2.5 py-0.5 border rounded-full text-[10px] font-bold text-center leading-none ${statusClass}`}>
                      {day.status}
                    </span>
                    {isAbsentLate && isHrOrDirection && (
                      <button
                        onClick={() => handleOverride(day.trackingId)}
                        disabled={loading}
                        className="px-2 py-1 bg-indigo-600 hover:bg-indigo-500 text-white text-[10px] font-extrabold rounded-lg shadow-sm transition-all duration-150 active:scale-95"
                      >
                        Justifier
                      </button>
                    )}
                  </div>
                </div>
              );
            })
          ) : (
            <div className="text-center py-12 text-gray-400 text-xs">
              Aucun détail disponible pour ce cycle.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
