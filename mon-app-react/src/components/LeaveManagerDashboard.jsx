import React, { useState, useEffect } from 'react';
import {
  Check,
  X,
  AlertCircle,
  Calendar,
  User,
  Clock,
  ShieldCheck,
  ThumbsUp,
  Inbox,
  UserCheck,
  UserX,
} from 'lucide-react';
import apiClient from '../api/apiClient';
import useAuthStore from '../store/useAuthStore';

// ---------------------------------------------------------------------------
// Helpers & constants
// ---------------------------------------------------------------------------

const LEAVE_TYPES = {
  ANNUAL: { label: 'Congé Annuel', bg: 'bg-blue-50 text-blue-700 border-blue-100' },
  SICK: { label: 'Maladie', bg: 'bg-rose-50 text-rose-700 border-rose-100' },
  UNPAID: { label: 'Sans Solde', bg: 'bg-slate-50 text-slate-700 border-slate-100' },
  MATERNITY: { label: 'Maternité', bg: 'bg-violet-50 text-violet-700 border-violet-100' },
  PATERNITY: { label: 'Paternité', bg: 'bg-indigo-50 text-indigo-700 border-indigo-100' },
  EXCEPTIONAL: { label: 'Congé Exceptionnel', bg: 'bg-amber-50 text-amber-700 border-amber-100' },
};

const STATUS_CONFIG = {
  PENDING: {
    label: 'En attente N+1',
    classes: 'bg-amber-50 text-amber-700 border-amber-200 ring-amber-100',
    dot: 'bg-amber-400 animate-pulse',
  },
  VALIDATED_N1: {
    label: 'Validé N+1',
    classes: 'bg-blue-50 text-blue-700 border-blue-200 ring-blue-100',
    dot: 'bg-blue-500',
  },
  PROCESSED_HR: {
    label: 'Traité RH',
    classes: 'bg-emerald-50 text-emerald-700 border-emerald-200 ring-emerald-100',
    dot: 'bg-emerald-500',
  },
  REJECTED: {
    label: 'Refusé',
    classes: 'bg-red-50 text-red-700 border-red-200 ring-red-100',
    dot: 'bg-red-500',
  },
};

// ---------------------------------------------------------------------------
// Main Component
// ---------------------------------------------------------------------------

export default function LeaveManagerDashboard({ onShowToast }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [stats, setStats] = useState({ pending: 0, approved: 0, rejected: 0 });
  
  // Destructure Zustand token and user details to ensure compliance with JWT auth guidelines
  const { token, user } = useAuthStore();

  // Fetch all leave requests from API
  const fetchRequests = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get('/api/v1/leaves');
      const allData = response.data || [];
      
      // Calculate stats based on all data
      setStats({
        pending: allData.filter(r => r.status === 'PENDING' || r.status === 'VALIDATED_N1').length,
        approved: allData.filter(r => r.status === 'PROCESSED_HR').length,
        rejected: allData.filter(r => r.status === 'REJECTED').length
      });

      // Filter for the validation list - show only requests in PENDING status
      setRequests(allData.filter(r => r.status === 'PENDING'));
    } catch (err) {
      console.error("Erreur lors du chargement des demandes de congés:", err);
      setError("Impossible de charger les demandes de congés.");
      if (onShowToast) {
        onShowToast("Erreur lors du chargement des demandes.", "error");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  // Stats calculation
  const totalPending = stats.pending;
  const totalApproved = stats.approved;
  const totalRejected = stats.rejected;

  const handleAction = async (id, action) => {
    const targetReq = requests.find(r => r.id === id);
    if (!targetReq) return;

    let newStatus = '';
    let userRole = 'MANAGER'; // Approving/rejecting pending requests requires the MANAGER role

    if (targetReq.status === 'PENDING') {
      newStatus = action === 'APPROVE' ? 'VALIDATED_N1' : 'REJECTED';
    } else {
      return; // Only pending requests are processed by the manager on this screen
    }

    try {
      const payload = {
        status: newStatus,
        comment: '',
        userRole: userRole
      };

      await apiClient.patch(`/api/v1/leaves/${id}/status`, payload);

      // Remove the treated leave request from the pending list
      setRequests(prev => prev.filter(req => req.id !== id));

      // Dynamically update stats cards locally
      setStats(prev => {
        const isApprove = action === 'APPROVE';
        return {
          pending: prev.pending - 1,
          approved: prev.approved, // Note: final approved status processed_hr is set by HR admin
          rejected: !isApprove ? prev.rejected + 1 : prev.rejected
        };
      });

      const employeeName = targetReq.employeeName || 'Collaborateur';
      if (onShowToast) {
        onShowToast(
          action === 'APPROVE'
            ? `La demande de congé de ${employeeName} a été validée.`
            : `La demande de congé de ${employeeName} a été refusée.`,
          action === 'APPROVE' ? 'success' : 'error'
        );
      }
    } catch (err) {
      console.error("Erreur lors de la mise à jour du congé:", err);
      // Log response details to help debug backend state validation issues
      if (err.response && err.response.data) {
        console.log("Détails de l'erreur renvoyés par Spring Boot:", err.response.data);
      }
      const errMsg = err.response?.data?.message || "Erreur serveur lors de la validation.";
      if (onShowToast) {
        onShowToast(`Échec de la validation : ${errMsg}`, "error");
      } else {
        alert(`Échec de la validation : ${errMsg}`);
      }
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  return (
    <div className="w-full min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/20 to-indigo-50/30 p-4 md:p-6 lg:p-8">
      
      {/* Header */}
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1.5">
            <div className="p-2.5 bg-blue-600 text-white rounded-2xl shadow-md shadow-blue-200">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold text-gray-900 tracking-tight">
                Console de Validation des Congés
              </h1>
              <p className="text-xs text-gray-400 font-medium mt-0.5">
                Espace réservé aux managers et administrateurs RH
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-8">
        {[
          {
            label: 'Demandes en attente',
            value: totalPending,
            sub: 'Nécessitent votre attention',
            icon: Clock,
            color: 'text-amber-600',
            bg: 'bg-amber-50 border-amber-100',
          },
          {
            label: 'Approuvées ce mois',
            value: totalApproved,
            sub: 'Traitées avec succès',
            icon: ThumbsUp,
            color: 'text-emerald-600',
            bg: 'bg-emerald-50 border-emerald-100',
          },
          {
            label: 'Refusées ce mois',
            value: totalRejected,
            sub: 'Non validées',
            icon: X,
            color: 'text-rose-600',
            bg: 'bg-rose-50 border-rose-100',
          },
        ].map(({ label, value, sub, icon: Icon, color, bg }) => (
          <div
            key={label}
            className={`bg-white/80 backdrop-blur-md border rounded-3xl p-5 flex items-start gap-4 shadow-sm hover:shadow-md transition-all duration-200 ${bg}`}
          >
            <div className={`p-3 rounded-2xl bg-white shadow-sm shrink-0 ${color}`}>
              <Icon className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-extrabold text-gray-900 leading-none">{value}</p>
              <p className="text-xs font-bold text-gray-700 mt-2">{label}</p>
              <p className="text-[11px] text-gray-400 mt-0.5">{sub}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Main Table Container */}
      <div className="bg-white/90 backdrop-blur-lg border border-gray-200/80 rounded-3xl shadow-xl overflow-hidden">
        {/* Table Title Bar */}
        <div className="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-1.5 bg-blue-50 text-blue-600 rounded-xl">
              <Inbox className="h-4 w-4" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-gray-900">Validation des Congés en attente</h2>
              <p className="text-[11px] text-gray-400 font-medium mt-0.5">
                Passez en revue les demandes soumises par votre équipe
              </p>
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100">
                <th className="py-3.5 px-6 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                  Collaborateur
                </th>
                <th className="py-3.5 px-6 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                  Type de Congé
                </th>
                <th className="py-3.5 px-6 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                  Période
                </th>
                <th className="py-3.5 px-6 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                  Motif
                </th>
                <th className="py-3.5 px-6 text-[11px] font-bold text-gray-400 uppercase tracking-wider">
                  Statut Actuel
                </th>
                <th className="py-3.5 px-6 text-[11px] font-bold text-gray-400 uppercase tracking-wider text-right">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {loading ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-gray-400">
                    <Clock className="h-8 w-8 mx-auto mb-2 text-blue-500 animate-spin" />
                    Chargement des demandes...
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-rose-500">
                    <AlertCircle className="h-8 w-8 mx-auto mb-2 text-rose-500" />
                    {error}
                  </td>
                </tr>
              ) : requests.length === 0 ? (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-gray-400">
                    <AlertCircle className="h-8 w-8 mx-auto mb-2 text-gray-300 animate-pulse" />
                    Aucune demande de congé enregistrée.
                  </td>
                </tr>
              ) : (
                requests.map(req => {
                  const initials = req.employeeName
                    .split(' ')
                    .map(n => n.charAt(0))
                    .join('')
                    .toUpperCase();

                  const typeCfg = LEAVE_TYPES[req.type] || { label: req.type, bg: 'bg-gray-50 text-gray-600' };
                  const statusCfg = STATUS_CONFIG[req.status] || {
                    label: req.status,
                    classes: 'bg-gray-50 text-gray-600 border-gray-200',
                    dot: 'bg-gray-400',
                  };

                  const isPending = req.status === 'PENDING' || req.status === 'VALIDATED_N1';

                  return (
                    <tr key={req.id} className="hover:bg-slate-50/40 transition-colors">
                      {/* Collaborateur */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="h-9 w-9 rounded-2xl bg-blue-50 border border-blue-100 flex items-center justify-center shrink-0 shadow-sm">
                            <span className="text-xs font-bold text-blue-700">{initials}</span>
                          </div>
                          <div>
                            <p className="font-bold text-gray-900 text-xs">{req.employeeName}</p>
                            <p className="text-[10px] text-gray-400 font-medium">{req.employeeEmail}</p>
                          </div>
                        </div>
                      </td>

                      {/* Type de Congé */}
                      <td className="py-4 px-6">
                        <span className={`inline-flex items-center text-[10px] font-bold px-2.5 py-1 rounded-xl border ${typeCfg.bg}`}>
                          {typeCfg.label}
                        </span>
                      </td>

                      {/* Période */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-1.5 text-xs text-gray-700">
                          <Calendar className="h-3.5 w-3.5 text-blue-500 shrink-0" />
                          <span className="font-semibold">
                            {formatDate(req.startDate)} — {formatDate(req.endDate)}
                          </span>
                        </div>
                      </td>

                      {/* Motif */}
                      <td className="py-4 px-6 max-w-xs">
                        <p className="text-xs text-gray-500 line-clamp-2 italic">
                          {req.reason || <span className="text-gray-300 not-italic">Non renseigné</span>}
                        </p>
                      </td>

                      {/* Statut Actuel */}
                      <td className="py-4 px-6">
                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${statusCfg.classes}`}>
                          <span className={`h-1.5 w-1.5 rounded-full shrink-0 ${statusCfg.dot}`} />
                          {statusCfg.label}
                        </span>
                      </td>

                      {/* Actions */}
                      <td className="py-4 px-6">
                        <div className="flex items-center justify-end gap-2">
                          {isPending ? (
                            <>
                              <button
                                onClick={() => handleAction(req.id, 'APPROVE')}
                                className="inline-flex items-center justify-center gap-1 px-3 py-1.5 text-xs font-bold text-emerald-700 hover:text-white bg-emerald-50 hover:bg-emerald-600 border border-emerald-200 hover:border-emerald-600 rounded-xl transition-all duration-150 shadow-sm hover:shadow active:scale-95"
                                title="Approuver la demande"
                              >
                                <Check className="h-3.5 w-3.5" />
                                <span>Valider</span>
                              </button>
                              <button
                                onClick={() => handleAction(req.id, 'REJECT')}
                                className="inline-flex items-center justify-center gap-1 px-3 py-1.5 text-xs font-bold text-rose-700 hover:text-white bg-rose-50 hover:bg-rose-600 border border-rose-200 hover:border-rose-600 rounded-xl transition-all duration-150 shadow-sm hover:shadow active:scale-95"
                                title="Refuser la demande"
                              >
                                <X className="h-3.5 w-3.5" />
                                <span>Refuser</span>
                              </button>
                            </>
                          ) : (
                            <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider px-2.5 py-1">
                              Traitée
                            </span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
