import React, { useState, useEffect } from 'react';
import useAuthStore from '../store/useAuthStore';
import apiClient from '../api/apiClient';
import TimecardGrid from './TimecardGrid';
import {
  Clock,
  Calendar,
  AlertCircle,
  CheckCircle,
  FileText,
  UserCheck,
  TrendingUp,
  Activity,
  Flame,
  CheckSquare,
  HelpCircle,
  ArrowRight,
  ShieldAlert,
  Server,
  Database,
  Cpu,
  Layers,
  Sparkles,
  User,
  Users,
  Compass,
  ArrowUpRight,
  RotateCw,
  Plus
} from 'lucide-react';

// ─── useAuth Hook ────────────────────────────────────────────────────────────
// Assumes userRole and userName from the active Zustand auth store.
export const useAuth = () => {
  const { user } = useAuthStore();
  const userRole = user?.roles?.[0] || 'EMPLOYEE';
  const userName = user?.name || `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || 'Collaborateur';
  return { userRole, userName };
};

// ─── Main DashboardView Component ──────────────────────────────────────────────
export default function DashboardView({ onShowToast, navigate }) {
  const { user } = useAuthStore();
  const { userRole, userName } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUserProfile = async () => {
      if (!user) {
        setLoading(false);
        return;
      }
      try {
        const res = await apiClient.get('/api/v1/hr/profiles');
        const myProfile = res.data.find(
          (p) => p.email && p.email.toLowerCase() === user.email.toLowerCase()
        );
        if (myProfile) {
          setProfile(myProfile);
          // Sync store with profileId if missing
          if (!user.profileId) {
            const { token: jwtToken, login: storeLogin } = useAuthStore.getState();
            storeLogin(jwtToken, { 
              ...user, 
              profileId: myProfile.id, 
              firstName: myProfile.firstName, 
              lastName: myProfile.lastName 
            });
          }
        }
      } catch (err) {
        console.error("Failed to load user profile in DashboardView:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchUserProfile();
  }, [user]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-slate-400 dark:text-slate-500 gap-2">
        <Clock className="h-6 w-6 text-indigo-650 animate-spin" />
        <span className="text-xs font-semibold">Chargement du tableau de bord...</span>
      </div>
    );
  }

  const isEmployee = userRole === 'EMPLOYEE';
  const department = profile?.department || '';
  const deptLower = department.toLowerCase();
  const isITorSupport = !isEmployee && (deptLower.includes('it') || deptLower.includes('support'));
  const isHRorDirection = !isEmployee && (deptLower.includes('ressources') || deptLower.includes('rh') || deptLower.includes('direction'));

  // Map the roles to reader-friendly labels
  const getRoleLabel = () => {
    switch (userRole) {
      case 'EMPLOYEE': return 'Collaborateur NEXUS';
      case 'MANAGER': return 'Manager d\'Équipe';
      case 'HR_ADMIN': return 'Administrateur Ressources Humaines';
      case 'DIRECTION': return 'Direction Générale';
      case 'IT_ADMIN': return 'Administrateur DSI / Système';
      default: return 'Utilisateur NEXUS';
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      
      {/* ─── Clean Welcoming Header (Transparent & Organic) ─── */}
      <div className="space-y-1 mb-6">
        <h1 className="text-3xl font-semibold tracking-tight text-slate-800 dark:text-slate-100">
          Bonjour, {userName}
        </h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm font-medium">
          {getRoleLabel()} {department && `• Département ${department}`}
        </p>
      </div>

      {/* ─── Main Grid Layout (1 col on mobile, 3 cols on desktop) ─── */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* IT & SUPPORT WORKSPACE */}
        {isITorSupport && (
          <>
            <QuickPunchWidget />
            <LeaveBalanceWidget />
            <MyTicketsWidget />
            <SystemHealthWidget />
            <div className="col-span-1 md:col-span-2">
              <OpenTicketsWidget />
            </div>
          </>
        )}

        {/* HR & DIRECTION WORKSPACE */}
        {isHRorDirection && (
          <>
            <div className="col-span-1 md:col-span-2">
              <AttritionRiskWidget />
            </div>
            <CompanyKpiWidget />
            <div className="col-span-1 md:col-span-3">
              <StaffShortcuts navigate={navigate} />
            </div>
          </>
        )}

        {/* DEFAULT/OTHER WORKSPACE */}
        {!isITorSupport && !isHRorDirection && (
          <>
            <QuickPunchWidget />
            <LeaveBalanceWidget />
            <MyTicketsWidget />
          </>
        )}

      </div>

      {/* Integrate TimecardGrid for IT/Support or default/other departments */}
      {(isITorSupport || (!isITorSupport && !isHRorDirection)) && (
        <div className="mt-8 animate-in fade-in slide-in-from-bottom-4 duration-350">
          <TimecardGrid employeeId={profile?.id} onShowToast={onShowToast} />
        </div>
      )}

    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// ─── PLACEHOLDER & MODULAR WIDGET COMPONENTS ─────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────

// 1. QuickPunchWidget
export function QuickPunchWidget() {
  const [time, setTime] = useState(new Date().toLocaleTimeString('fr-FR'));
  const [punched, setPunched] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const timer = setInterval(() => {
      setTime(new Date().toLocaleTimeString('fr-FR'));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const handlePunch = () => {
    setLoading(true);
    setTimeout(() => {
      setPunched(!punched);
      setLoading(false);
    }, 800);
  };

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm">Pointeuse Virtuelle</h3>
          <span className={`inline-flex h-2 w-2 rounded-full ${punched ? 'bg-emerald-500 animate-pulse' : 'bg-slate-300 dark:bg-slate-600'}`} />
        </div>
        
        <div className="text-center py-3">
          <div className="text-3xl font-mono font-bold text-slate-800 dark:text-slate-100 tracking-wider">
            {time}
          </div>
          <p className="text-[11px] text-slate-400 dark:text-slate-500 font-medium mt-0.5">
            {new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })}
          </p>
        </div>
      </div>

      <div className="mt-3 space-y-2.5">
        <div className="bg-slate-50 dark:bg-slate-900 border border-slate-100 dark:border-slate-700/50 rounded-lg p-2.5 text-center">
          <p className="text-[10px] text-slate-400 dark:text-slate-500 uppercase font-semibold">Statut actuel</p>
          <p className="text-xs font-bold text-slate-700 dark:text-slate-350 mt-0.5">
            {punched ? '🟢 Travail en cours' : '🔴 Non pointé (En attente)'}
          </p>
        </div>

        <button
          onClick={handlePunch}
          disabled={loading}
          className={`w-full py-2 rounded-lg text-xs font-semibold tracking-wide transition-all flex items-center justify-center gap-1.5 active:scale-98
            ${punched 
              ? 'bg-red-50 dark:bg-red-950/20 text-red-650 dark:text-red-400 hover:bg-red-100 border border-red-100 dark:border-red-900/40' 
              : 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-sm'}`}
        >
          {loading ? (
            <span className="h-3.5 w-3.5 border-2 border-current border-t-transparent rounded-full animate-spin" />
          ) : punched ? (
            'Enregistrer Départ'
          ) : (
            'Enregistrer Arrivée'
          )}
        </button>
      </div>
    </div>
  );
}

// 2. LeaveBalanceWidget
export function LeaveBalanceWidget() {
  const balances = [
    { label: 'Congés Payés', current: 18, total: 25, color: 'from-indigo-500 to-blue-500' },
    { label: 'RTT', current: 4, total: 8, color: 'from-violet-500 to-purple-500' },
    { label: 'Congés Maladie', current: 0, total: 10, color: 'from-emerald-500 to-teal-500' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      <div>
        <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-4">Mes Soldes de Congés</h3>
        
        <div className="space-y-3.5">
          {balances.map((b) => {
            const pct = (b.current / b.total) * 100;
            return (
              <div key={b.label} className="space-y-1">
                <div className="flex justify-between items-center text-xs">
                  <span className="font-medium text-slate-700 dark:text-slate-350">{b.label}</span>
                  <span className="font-bold text-slate-800 dark:text-slate-200">{b.current} / {b.total} j</span>
                </div>
                <div className="h-1.5 w-full bg-slate-100 dark:bg-slate-900 border border-slate-50 dark:border-slate-800 rounded-full overflow-hidden">
                  <div 
                    className={`h-full bg-gradient-to-r ${b.color} rounded-full transition-all duration-500`}
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-700/80 flex items-center justify-between">
        <span className="text-[10px] text-slate-400 dark:text-slate-500">Mis à jour ce matin</span>
        <button className="text-xs font-bold text-indigo-650 dark:text-indigo-400 hover:underline inline-flex items-center gap-0.5">
          Détails
          <ArrowRight className="h-3.5 w-3.5" />
        </button>
      </div>
    </div>
  );
}

// 3. MyTicketsWidget
export function MyTicketsWidget() {
  const tickets = [
    { id: 'TKT-102', subject: 'Matériel ergonomique requis', status: 'RESOLVED', priority: 'LOW' },
    { id: 'TKT-105', subject: 'Problème de connexion VPN', status: 'OPEN', priority: 'HIGH' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      <div>
        <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">Demandes d'Assistance</h3>
        
        <div className="space-y-2.5">
          {tickets.map((t) => (
            <div key={t.id} className="bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-700/30 rounded-lg p-2.5 flex items-center justify-between">
              <div className="min-w-0 pr-2">
                <span className="text-[9px] font-mono font-bold text-slate-400 dark:text-slate-500">#{t.id}</span>
                <p className="text-xs font-bold text-slate-700 dark:text-slate-350 truncate mt-0.5">{t.subject}</p>
              </div>
              <div className="flex items-center gap-1.5 shrink-0">
                <span className={`text-[8.5px] font-bold px-1.5 py-0.5 rounded border
                  ${t.status === 'RESOLVED' 
                    ? 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-450 border-emerald-100 dark:border-emerald-900/40' 
                    : 'bg-amber-50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-450 border-amber-100 dark:border-amber-900/40'}`}>
                  {t.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="mt-3.5">
        <button className="w-full py-1.5 bg-slate-50 dark:bg-slate-900 hover:bg-slate-100 dark:hover:bg-slate-950 border border-slate-200 dark:border-slate-700 rounded-lg text-xs font-semibold text-slate-600 dark:text-slate-400 transition-colors">
          Créer un ticket support
        </button>
      </div>
    </div>
  );
}

// 4. PendingApprovalsWidget
export function PendingApprovalsWidget() {
  const [requests, setRequests] = useState([
    { id: 1, name: 'Julien Morel', type: 'Congés Payés', duration: '3 jours', date: '18 au 21 Juin' },
    { id: 2, name: 'Jane Doe', type: 'RTT', duration: '1 jour', date: '25 Juin' }
  ]);

  const handleAction = (id, approved) => {
    setRequests(requests.filter(r => r.id !== id));
  };

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all h-full flex flex-col justify-between">
      <div>
        <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">Demandes de Congés en Attente</h3>
        
        {requests.length === 0 ? (
          <div className="text-center py-6 text-slate-400 dark:text-slate-500">
            <CheckCircle className="h-7 w-7 mx-auto mb-1.5 text-emerald-500" />
            <p className="text-xs font-medium">Toutes les validations sont à jour !</p>
          </div>
        ) : (
          <div className="space-y-2.5">
            {requests.map((r) => (
              <div key={r.id} className="bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-700/30 rounded-lg p-2.5 flex flex-wrap items-center justify-between gap-2.5">
                <div>
                  <p className="text-xs font-bold text-slate-750 dark:text-slate-300">{r.name}</p>
                  <p className="text-[10px] text-slate-500 dark:text-slate-450 mt-0.5">{r.type} · {r.duration} ({r.date})</p>
                </div>
                <div className="flex gap-1.5 shrink-0">
                  <button 
                    onClick={() => handleAction(r.id, false)}
                    className="px-2 py-1 rounded border border-red-200 dark:border-red-900/40 bg-red-50/50 dark:bg-red-950/20 hover:bg-red-100 dark:hover:bg-red-950 text-red-650 dark:text-red-400 transition-colors text-[10px]"
                  >
                    Refuser
                  </button>
                  <button 
                    onClick={() => handleAction(r.id, true)}
                    className="px-2 py-1 rounded border border-emerald-200 dark:border-emerald-900/40 bg-emerald-50/50 dark:bg-emerald-950/20 hover:bg-emerald-100 dark:hover:bg-emerald-950 text-emerald-650 dark:text-emerald-400 transition-colors text-[10px] font-semibold"
                  >
                    Approuver
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// 5. TeamAttendanceHeatmap
export function TeamAttendanceHeatmap() {
  const team = [
    { name: 'Julien Morel', status: 'PRESENT', time: '08:54', label: 'Présent' },
    { name: 'Jane Doe', status: 'LEAVE', time: '-', label: 'En congé' },
    { name: 'Paul Martin', status: 'LATE', time: '09:12', label: 'En retard (12m)' },
    { name: 'Sophie Laurent', status: 'PRESENT', time: '08:45', label: 'Présent' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">Présence Équipe (Aujourd'hui)</h3>
      
      <div className="space-y-2.5">
        {team.map((t) => (
          <div key={t.name} className="flex items-center justify-between p-1.5 hover:bg-slate-50 dark:hover:bg-slate-900 rounded-lg transition-colors">
            <div className="flex items-center gap-2 min-w-0">
              <span className={`h-2 w-2 rounded-full shrink-0
                ${t.status === 'PRESENT' ? 'bg-emerald-500' : ''}
                ${t.status === 'LEAVE' ? 'bg-blue-500' : ''}
                ${t.status === 'LATE' ? 'bg-amber-500' : ''}`} 
              />
              <p className="text-xs font-semibold text-slate-750 dark:text-slate-300 truncate">{t.name}</p>
            </div>
            <div className="text-right">
              <p className="text-[10px] font-bold text-slate-700 dark:text-slate-400">{t.label}</p>
              <p className="text-[9px] text-slate-400 dark:text-slate-500">{t.time !== '-' ? `Pointé à ${t.time}` : ''}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// 6. AttritionRiskWidget
export function AttritionRiskWidget() {
  const [age, setAge] = useState(25);
  const [absencesCount, setAbsencesCount] = useState(15);
  const [overtimeHours, setOvertimeHours] = useState(0);
  const [performanceScore, setPerformanceScore] = useState(40);
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const triggerPrediction = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await apiClient.get('/api/v1/analytics/attrition-risk', {
        params: { age, absencesCount, overtimeHours, performanceScore }
      });
      setPrediction(res.data);
    } catch (err) {
      console.error(err);
      setError("Erreur de connexion au modèle ML");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all">
      <div className="flex items-center justify-between mb-4 pb-2 border-b border-slate-100 dark:border-slate-700/85">
        <div>
          <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm">Simulateur d'Attrition (Smile ML Random Forest)</h3>
          <p className="text-[10px] text-slate-400 dark:text-slate-500 mt-0.5">Modèle décisionnel prédictif basé sur 4 variables</p>
        </div>
        <span className="p-1 px-2.5 bg-blue-50 dark:bg-blue-950/20 text-blue-700 dark:text-blue-400 text-[9px] font-bold rounded-lg border border-blue-100 dark:border-blue-900/30 flex items-center gap-1.5">
          <Sparkles className="h-3 w-3 text-amber-500" />
          Real-Time Prediction
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Sliders */}
        <div className="space-y-3">
          <div className="space-y-1">
            <div className="flex justify-between text-xs font-semibold text-slate-700 dark:text-slate-350">
              <span>Âge du collaborateur</span>
              <span className="text-indigo-600 dark:text-indigo-400">{age} ans</span>
            </div>
            <input 
              type="range" min="18" max="65" value={age} 
              onChange={e => setAge(parseInt(e.target.value))}
              className="w-full accent-indigo-650 h-1 bg-slate-200 dark:bg-slate-700 rounded-full appearance-none cursor-pointer focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <div className="flex justify-between text-xs font-semibold text-slate-700 dark:text-slate-350">
              <span>Nombre d'absences</span>
              <span className="text-indigo-600 dark:text-indigo-400">{absencesCount} jours</span>
            </div>
            <input 
              type="range" min="0" max="30" value={absencesCount} 
              onChange={e => setAbsencesCount(parseInt(e.target.value))}
              className="w-full accent-indigo-655 h-1 bg-slate-200 dark:bg-slate-700 rounded-full appearance-none cursor-pointer focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <div className="flex justify-between text-xs font-semibold text-slate-700 dark:text-slate-350">
              <span>Heures supplémentaires</span>
              <span className="text-indigo-600 dark:text-indigo-400">{overtimeHours} h</span>
            </div>
            <input 
              type="range" min="0" max="80" value={overtimeHours} 
              onChange={e => setOvertimeHours(parseInt(e.target.value))}
              className="w-full accent-indigo-650 h-1 bg-slate-200 dark:bg-slate-700 rounded-full appearance-none cursor-pointer focus:outline-none"
            />
          </div>

          <div className="space-y-1">
            <div className="flex justify-between text-xs font-semibold text-slate-700 dark:text-slate-350">
              <span>Score de Performance</span>
              <span className="text-indigo-600 dark:text-indigo-400">{performanceScore} / 100</span>
            </div>
            <input 
              type="range" min="1" max="100" value={performanceScore} 
              onChange={e => setPerformanceScore(parseInt(e.target.value))}
              className="w-full accent-indigo-650 h-1 bg-slate-200 dark:bg-slate-700 rounded-full appearance-none cursor-pointer focus:outline-none"
            />
          </div>

          <button
            onClick={triggerPrediction}
            disabled={loading}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-1.5 rounded-lg text-xs transition-colors flex items-center justify-center gap-1.5 mt-3 shadow-sm active:scale-98"
          >
            {loading ? (
              <span className="h-3.5 w-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <RotateCw className="h-3.5 w-3.5" />
                Lancer la Prévision ML
              </>
            )}
          </button>
        </div>

        {/* Results Panel */}
        <div className="bg-slate-50 dark:bg-slate-900 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4 flex flex-col justify-between min-h-[180px]">
          <div className="space-y-1">
            <h4 className="text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-wider">Résultat du Modèle</h4>
            <p className="text-[9px] text-slate-400 dark:text-slate-500">Classifieur Smile RandomForest</p>
          </div>

          <div className="my-3 flex-1 flex items-center justify-center">
            {loading ? (
              <div className="flex flex-col items-center gap-1.5 text-center">
                <span className="h-5 w-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
                <p className="text-[10px] font-semibold text-slate-400 dark:text-slate-500">Extraction des features & prédiction...</p>
              </div>
            ) : prediction ? (
              prediction === 'HIGH_RISK' ? (
                <div className="text-center bg-red-50/50 dark:bg-red-950/20 border border-red-200/60 dark:border-red-900/30 rounded-xl p-3 w-full animate-in zoom-in duration-200">
                  <Flame className="h-6 w-6 text-red-500 mx-auto animate-bounce" />
                  <p className="text-xs font-bold text-red-650 dark:text-red-400 mt-1.5">RISQUE ÉLEVÉ (HIGH_RISK)</p>
                  <p className="text-[9px] text-red-450 dark:text-red-500/80 mt-0.5">Le profil présente des facteurs d'attrition critiques.</p>
                </div>
              ) : (
                <div className="text-center bg-green-50/50 dark:bg-green-950/20 border border-green-200/60 dark:border-green-900/30 rounded-xl p-3 w-full animate-in zoom-in duration-200">
                  <CheckCircle className="h-6 w-6 text-green-500 mx-auto" />
                  <p className="text-xs font-bold text-green-655 dark:text-green-450 mt-1.5">RISQUE FAIBLE (LOW_RISK)</p>
                  <p className="text-[9px] text-green-450 dark:text-green-500/80 mt-0.5">Le profil est identifié comme stable et engagé.</p>
                </div>
              )
            ) : error ? (
              <p className="text-xs text-red-500 text-center font-semibold">{error}</p>
            ) : (
              <div className="flex flex-col items-center justify-center text-center py-2">
                <Activity className="h-7 w-7 text-slate-350 dark:text-slate-600 mb-2 animate-pulse" />
                <p className="text-[11px] text-slate-400 dark:text-slate-505 font-medium max-w-[180px]">
                  Ajustez les paramètres pour lancer la simulation
                </p>
              </div>
            )}
          </div>

          <div className="text-[9px] text-slate-400 dark:text-slate-500 text-center border-t border-slate-200/80 dark:border-slate-700/80 pt-2 font-mono">
            NEXUS ML Engine
          </div>
        </div>

      </div>
    </div>
  );
}

// 7. CompanyKpiWidget
export function CompanyKpiWidget() {
  const kpis = [
    { label: 'Taux de présence global', value: '94.2%', color: 'text-emerald-600 dark:text-emerald-450' },
    { label: 'Taux de rotation annuel', value: '5.4%', color: 'text-indigo-650 dark:text-indigo-400' },
    { label: 'Alertes Formation active', value: '3', color: 'text-amber-600 dark:text-amber-455' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      <div>
        <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">Indicateurs RH Globaux</h3>
        
        <div className="space-y-3">
          {kpis.map((k) => (
            <div key={k.label} className="flex items-center justify-between p-2 bg-slate-50 dark:bg-slate-900 border border-slate-100 dark:border-slate-750 rounded-lg">
              <span className="text-xs font-semibold text-slate-600 dark:text-slate-400">{k.label}</span>
              <span className={`text-xs font-bold ${k.color}`}>{k.value}</span>
            </div>
          ))}
        </div>
      </div>
      
      <div className="mt-4 pt-2 border-t border-slate-100 dark:border-slate-700/80">
        <span className="text-[10px] text-slate-400 dark:text-slate-500">Statistiques temps réel</span>
      </div>
    </div>
  );
}

// 8. StaffShortcuts
export function StaffShortcuts({ navigate }) {
  const shortcuts = [
    { title: 'Fiches Employés', desc: 'Consulter la liste', action: 'Gérer', icon: Users, path: '/profils' },
    { title: 'Coffre-fort Documents', desc: 'Gestion administrative', action: 'Ouvrir', icon: FileText, path: '/documents' },
    { title: 'Tableau de Validation', desc: 'Congés à traiter', action: 'Valider', icon: CheckSquare, path: '/validation-conges' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all font-sans">
      <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">Actions Rapides</h3>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {shortcuts.map((s) => {
          const Icon = s.icon;
          return (
            <div 
              key={s.title} 
              onClick={() => navigate && navigate(s.path)}
              className="bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-700/30 rounded-xl p-4 flex items-start justify-between group transition-all cursor-pointer hover:bg-slate-100/50 dark:hover:bg-slate-900 transition-colors"
            >
              <div className="space-y-2">
                <div className="p-2 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 inline-block text-indigo-650 dark:text-indigo-400">
                  <Icon className="h-4 w-4" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-750 dark:text-slate-300">{s.title}</h4>
                  <p className="text-[10px] text-slate-450 dark:text-slate-500 mt-0.5">{s.desc}</p>
                </div>
              </div>
              <span className="text-[10px] font-bold text-indigo-650 dark:text-indigo-400 group-hover:underline">
                {s.action}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// 9. SystemHealthWidget
export function SystemHealthWidget() {
  const metrics = [
    { label: 'Statut du Serveur', value: 'Running', details: 'OK', type: 'status' },
    { label: 'Base de données', value: 'MySQL 8.0', details: 'Connected', type: 'db' },
    { label: 'Utilisation Mémoire', value: '1.4 / 4.0 GB', details: '35%', type: 'ram' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
      <div>
        <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">Santé Système & APIs</h3>
        
        <div className="space-y-3">
          {metrics.map((m) => (
            <div key={m.label} className="bg-slate-50 dark:bg-slate-900 border border-slate-100 dark:border-slate-750 rounded-lg p-2.5 flex items-center justify-between">
              <div>
                <p className="text-[10px] text-slate-450 dark:text-slate-500 font-medium">{m.label}</p>
                <p className="text-xs font-bold text-slate-700 dark:text-slate-300 mt-0.5">{m.value}</p>
              </div>
              <span className={`text-[8.5px] font-bold px-2 py-0.5 rounded border
                ${m.type === 'status' || m.type === 'db' 
                  ? 'bg-emerald-50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-450 border-emerald-100 dark:border-emerald-900/40' 
                  : 'bg-indigo-50 dark:bg-indigo-950/20 text-indigo-700 dark:text-indigo-400 border-indigo-100 dark:border-indigo-900/40'}`}>
                {m.details}
              </span>
            </div>
          ))}
        </div>
      </div>
      
      <div className="mt-4 pt-2 border-t border-slate-100 dark:border-slate-700/80 flex items-center gap-1.5">
        <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
        <span className="text-[10px] text-slate-450 dark:text-slate-500">Tous les services opérationnels</span>
      </div>
    </div>
  );
}

// 10. OpenTicketsWidget
export function OpenTicketsWidget() {
  const itTickets = [
    { id: 'TKT-990', title: 'Accès VPN bloqué pour Julien', severity: 'CRITICAL', time: 'SLA: 1h 24m' },
    { id: 'TKT-991', title: 'Ajuster les règles CORS de production', severity: 'HIGH', time: 'SLA: 12h 45m' },
    { id: 'TKT-992', title: 'Swagger UI ne charge pas les schémas', severity: 'MEDIUM', time: 'SLA: 22h 10m' }
  ];

  return (
    <div className="bg-white dark:bg-slate-800 border border-slate-200/80 dark:border-slate-700/80 rounded-xl p-4.5 shadow-sm hover:shadow-md transition-all">
      <h3 className="font-semibold text-slate-800 dark:text-slate-200 text-sm mb-3">File d'Attente DSI (Support IT)</h3>
      
      <div className="space-y-2.5">
        {itTickets.map((t) => (
          <div key={t.id} className="bg-slate-50 dark:bg-slate-900/50 border border-slate-100 dark:border-slate-700/30 rounded-lg p-2.5 flex items-center justify-between flex-wrap gap-2">
            <div>
              <span className="text-[9px] font-mono font-bold text-slate-450 dark:text-slate-500">#{t.id}</span>
              <p className="text-xs font-bold text-slate-750 dark:text-slate-350 mt-0.5">{t.title}</p>
              <p className="text-[9px] text-slate-400 dark:text-slate-500 mt-0.5">{t.time}</p>
            </div>
            
            <div className="flex items-center gap-2 shrink-0">
              <span className={`text-[8.5px] font-bold px-1.5 py-0.5 rounded border
                ${t.severity === 'CRITICAL' ? 'bg-red-50 dark:bg-red-950/20 text-red-700 dark:text-red-450 border-red-100 dark:border-red-900/45 animate-pulse' : ''}
                ${t.severity === 'HIGH' ? 'bg-orange-50 dark:bg-orange-950/20 text-orange-700 dark:text-orange-450 border-orange-100 dark:border-orange-900/45' : ''}
                ${t.severity === 'MEDIUM' ? 'bg-amber-50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-450 border-amber-100 dark:border-amber-900/45' : ''}`}>
                {t.severity}
              </span>
              <button className="text-[10px] font-bold text-indigo-650 dark:text-indigo-400 hover:underline">
                Résoudre
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
