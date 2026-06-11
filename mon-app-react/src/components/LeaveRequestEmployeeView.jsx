import React, { useState, useEffect, useCallback } from 'react';
import {
  CalendarDays,
  Send,
  RefreshCw,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  BadgeCheck,
  ChevronDown,
  FileText,
  Inbox,
  RotateCcw,
} from 'lucide-react';
import useAuthStore from '../store/useAuthStore';
import apiClient from '../api/apiClient';

// ---------------------------------------------------------------------------
// Helpers & constants
// ---------------------------------------------------------------------------

const LEAVE_TYPES = [
  { value: 'ANNUAL', label: 'Congé Annuel' },
  { value: 'SICK', label: 'Maladie' },
  { value: 'UNPAID', label: 'Sans Solde' },
  { value: 'MATERNITY', label: 'Maternité' },
  { value: 'PATERNITY', label: 'Paternité' },
  { value: 'EXCEPTIONAL', label: 'Congé Exceptionnel' },
];

/**
 * Returns Tailwind badge classes and a human-readable label for each backend status.
 * Statuses: PENDING | VALIDATED_N1 | PROCESSED_HR | REJECTED
 */
const STATUS_CONFIG = {
  PENDING: {
    label: 'En attente',
    icon: Clock,
    classes: 'bg-amber-50 text-amber-700 border border-amber-200 ring-1 ring-amber-100',
    dot: 'bg-amber-400 animate-pulse',
  },
  VALIDATED_N1: {
    label: 'Validé N+1',
    icon: BadgeCheck,
    classes: 'bg-blue-50 text-blue-700 border border-blue-200 ring-1 ring-blue-100',
    dot: 'bg-blue-500',
  },
  PROCESSED_HR: {
    label: 'Traité RH',
    icon: CheckCircle2,
    classes: 'bg-emerald-50 text-emerald-700 border border-emerald-200 ring-1 ring-emerald-100',
    dot: 'bg-emerald-500',
  },
  REJECTED: {
    label: 'Refusé',
    icon: XCircle,
    classes: 'bg-red-50 text-red-700 border border-red-200 ring-1 ring-red-100',
    dot: 'bg-red-500',
  },
};

const DEFAULT_STATUS = {
  label: 'Inconnu',
  icon: AlertCircle,
  classes: 'bg-gray-50 text-gray-600 border border-gray-200',
  dot: 'bg-gray-400',
};

// ---------------------------------------------------------------------------
// API functions
// ---------------------------------------------------------------------------

/**
 * Submit a new leave request.
 * @param {number} employeeId
 * @param {{ startDate: string, endDate: string, type: string, reason: string }} data
 */
async function submitLeaveRequest(employeeId, data) {
  const response = await apiClient.post(`/api/v1/leaves?employeeId=${employeeId}`, data);
  return response.data;
}

/**
 * Fetch the authenticated employee's own leave requests by querying all leaves and filtering by email.
 * @returns {Promise<Array>}
 */
async function fetchMyLeaveRequests() {
  const user = useAuthStore.getState().user;
  if (!user || !user.email) return [];
  const response = await apiClient.get('/api/v1/leaves');
  return (response.data || []).filter(
    (r) => r.employeeEmail && r.employeeEmail.toLowerCase() === user.email.toLowerCase()
  );
}

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

/** Dynamic status badge */
function StatusBadge({ status }) {
  const config = STATUS_CONFIG[status] ?? DEFAULT_STATUS;
  const Icon = config.icon;
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${config.classes}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full shrink-0 ${config.dot}`} />
      <Icon className="h-3 w-3 shrink-0" />
      {config.label}
    </span>
  );
}

/** Single leave request card used in the history list */
function LeaveRequestCard({ request }) {
  const leaveTypeLabel =
    LEAVE_TYPES.find((t) => t.value === request.type)?.label ?? request.type;

  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  };

  // Calculate number of calendar days
  const calcDays = () => {
    if (!request.startDate || !request.endDate) return null;
    const diff =
      (new Date(request.endDate) - new Date(request.startDate)) /
      (1000 * 60 * 60 * 24);
    return Math.max(1, Math.round(diff) + 1);
  };

  const days = calcDays();

  return (
    <div className="group relative bg-white/70 backdrop-blur-sm border border-gray-200/80 rounded-2xl p-4 shadow-sm hover:shadow-md hover:border-blue-200/70 transition-all duration-200">
      {/* Top row */}
      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <p className="text-sm font-bold text-gray-800">{leaveTypeLabel}</p>
          {days !== null && (
            <p className="text-[11px] text-gray-400 font-medium mt-0.5">
              {days} jour{days > 1 ? 's' : ''}
            </p>
          )}
        </div>
        <StatusBadge status={request.status} />
      </div>

      {/* Dates */}
      <div className="flex items-center gap-2 text-xs text-gray-500 mb-3">
        <CalendarDays className="h-3.5 w-3.5 text-blue-400 shrink-0" />
        <span>
          {formatDate(request.startDate)} → {formatDate(request.endDate)}
        </span>
      </div>

      {/* Reason */}
      {request.reason && (
        <p className="text-xs text-gray-500 italic line-clamp-2 border-t border-gray-100 pt-2 mt-1">
          {request.reason}
        </p>
      )}
    </div>
  );
}

// ---------------------------------------------------------------------------
// Main component
// ---------------------------------------------------------------------------

/**
 * LeaveRequestEmployeeView
 *
 * Allows an authenticated employee to:
 *  1. Submit a new leave request via a form.
 *  2. View their own leave request history with dynamic status badges.
 *
 * Props:
 *  - onShowToast(message, type) — optional callback for global toast notifications.
 */
export default function LeaveRequestEmployeeView({ onShowToast }) {
  const { user } = useAuthStore();

  // ── Form state ────────────────────────────────────────────────────────────
  const [form, setForm] = useState({
    startDate: '',
    endDate: '',
    type: '',
    reason: '',
  });
  const [formSubmitting, setFormSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);

  // ── History state ─────────────────────────────────────────────────────────
  const [requests, setRequests] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);
  const [historyError, setHistoryError] = useState(null);

  // ── Helpers ───────────────────────────────────────────────────────────────
  const showToast = useCallback(
    (message, type = 'info') => {
      if (onShowToast) onShowToast(message, type);
    },
    [onShowToast]
  );

  const loadHistory = useCallback(async (silent = false) => {
    if (!silent) setHistoryLoading(true);
    setHistoryError(null);
    try {
      const data = await fetchMyLeaveRequests();
      setRequests(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Erreur chargement historique congés :', err);
      setHistoryError("Impossible de charger l'historique des demandes.");
    } finally {
      setHistoryLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user) loadHistory();
  }, [user, loadHistory]);

  // ── Form handlers ─────────────────────────────────────────────────────────
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (formError) setFormError(null);
  };

  const validate = () => {
    if (!form.startDate) return 'La date de début est requise.';
    if (!form.endDate) return 'La date de fin est requise.';
    if (new Date(form.endDate) < new Date(form.startDate))
      return 'La date de fin doit être postérieure à la date de début.';
    if (!form.type) return 'Veuillez sélectionner un type de congé.';
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationError = validate();
    if (validationError) {
      setFormError(validationError);
      return;
    }

    setFormSubmitting(true);
    setFormError(null);

    try {
      // Fetch the profiles to dynamically match the current user's profile ID
      const profilesRes = await apiClient.get('/api/v1/hr/profiles');
      const myProfile = profilesRes.data.find(
        (p) => p.email && p.email.toLowerCase() === user.email.toLowerCase()
      );

      if (!myProfile) {
        throw new Error("Impossible de trouver votre profil collaborateur.");
      }

      await submitLeaveRequest(myProfile.id, {
        startDate: form.startDate,
        endDate: form.endDate,
        type: form.type,
        reason: form.reason,
      });

      showToast('Demande de congé soumise avec succès !', 'success');

      // Reset form
      setForm({ startDate: '', endDate: '', type: '', reason: '' });

      // Refresh history silently
      await loadHistory(true);
    } catch (err) {
      console.error('Erreur soumission congé :', err);
      const serverMsg = err.response?.data?.message || err.message;
      const msg = serverMsg || 'Une erreur est survenue. Veuillez réessayer.';
      setFormError(msg);
      showToast(msg, 'error');
    } finally {
      setFormSubmitting(false);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <div className="w-full min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/40 p-4 md:p-6 lg:p-8">

      {/* Page header */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-1.5">
          <div className="p-2.5 bg-blue-600 text-white rounded-2xl shadow-md shadow-blue-200">
            <CalendarDays className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-xl font-extrabold text-gray-900 tracking-tight">
              Demandes de Congés
            </h1>
            <p className="text-xs text-gray-400 font-medium mt-0.5">
              {user?.firstName && user?.lastName
                ? `${user.firstName} ${user.lastName}`
                : user?.email ?? 'Employé'}
            </p>
          </div>
        </div>
      </div>

      {/* Two-column layout on large screens */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 xl:gap-8 items-start">

        {/* ═══════════════════════════════════════════════════════════════════
            SECTION 1 — Request form  (2/5 on lg)
        ══════════════════════════════════════════════════════════════════ */}
        <div className="lg:col-span-2">
          <div className="relative bg-white/80 backdrop-blur-lg border border-white rounded-3xl shadow-xl overflow-hidden">

            {/* Decorative blobs */}
            <div className="absolute -top-8 -right-8 w-32 h-32 bg-blue-100/40 rounded-full blur-3xl pointer-events-none" />
            <div className="absolute -bottom-8 -left-8 w-32 h-32 bg-indigo-100/40 rounded-full blur-3xl pointer-events-none" />

            <div className="relative z-10 p-6">
              {/* Card header */}
              <div className="flex items-center gap-2.5 mb-6 pb-4 border-b border-gray-100">
                <div className="p-1.5 bg-blue-50 text-blue-600 rounded-xl">
                  <FileText className="h-4 w-4" />
                </div>
                <div>
                  <h2 className="text-sm font-bold text-gray-900">Nouvelle Demande</h2>
                  <p className="text-[11px] text-gray-400 font-medium mt-0.5">
                    Remplissez le formulaire ci-dessous
                  </p>
                </div>
              </div>

              <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">

                {/* Date range */}
                <div className="grid grid-cols-2 gap-3">
                  <div className="flex flex-col gap-1.5">
                    <label
                      htmlFor="startDate"
                      className="text-[11px] font-bold text-gray-500 uppercase tracking-wider"
                    >
                      Date de début
                    </label>
                    <input
                      id="startDate"
                      name="startDate"
                      type="date"
                      value={form.startDate}
                      onChange={handleChange}
                      required
                      min={new Date().toISOString().split('T')[0]}
                      className="w-full rounded-xl border border-gray-200 bg-gray-50/60 px-3 py-2.5 text-sm text-gray-800 font-medium outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all duration-150"
                    />
                  </div>

                  <div className="flex flex-col gap-1.5">
                    <label
                      htmlFor="endDate"
                      className="text-[11px] font-bold text-gray-500 uppercase tracking-wider"
                    >
                      Date de fin
                    </label>
                    <input
                      id="endDate"
                      name="endDate"
                      type="date"
                      value={form.endDate}
                      onChange={handleChange}
                      required
                      min={form.startDate || new Date().toISOString().split('T')[0]}
                      className="w-full rounded-xl border border-gray-200 bg-gray-50/60 px-3 py-2.5 text-sm text-gray-800 font-medium outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all duration-150"
                    />
                  </div>
                </div>

                {/* Leave type */}
                <div className="flex flex-col gap-1.5">
                  <label
                    htmlFor="type"
                    className="text-[11px] font-bold text-gray-500 uppercase tracking-wider"
                  >
                    Type de congé
                  </label>
                  <div className="relative">
                    <select
                      id="type"
                      name="type"
                      value={form.type}
                      onChange={handleChange}
                      required
                      className="w-full appearance-none rounded-xl border border-gray-200 bg-gray-50/60 px-3 py-2.5 pr-9 text-sm text-gray-800 font-medium outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all duration-150"
                    >
                      <option value="" disabled>
                        Sélectionner un type...
                      </option>
                      {LEAVE_TYPES.filter((lt) => {
                        if (lt.value === 'SICK') {
                          return user?.roles?.some((r) => ['HR_ADMIN', 'DIRECTION'].includes(r));
                        }
                        return true;
                      }).map((lt) => (
                        <option key={lt.value} value={lt.value}>
                          {lt.label}
                        </option>
                      ))}
                    </select>
                    <ChevronDown className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                  </div>
                </div>

                {/* Reason */}
                <div className="flex flex-col gap-1.5">
                  <label
                    htmlFor="reason"
                    className="text-[11px] font-bold text-gray-500 uppercase tracking-wider"
                  >
                    Motif{' '}
                    <span className="normal-case font-normal text-gray-400">(optionnel)</span>
                  </label>
                  <textarea
                    id="reason"
                    name="reason"
                    value={form.reason}
                    onChange={handleChange}
                    rows={3}
                    placeholder="Décrivez brièvement le motif de votre demande..."
                    className="w-full resize-none rounded-xl border border-gray-200 bg-gray-50/60 px-3 py-2.5 text-sm text-gray-800 placeholder:text-gray-300 font-medium outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all duration-150"
                  />
                </div>

                {/* Inline validation error */}
                {formError && (
                  <div className="flex items-start gap-2 bg-red-50 border border-red-200 rounded-xl px-3 py-2.5 text-xs text-red-600 font-medium">
                    <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-red-500" />
                    {formError}
                  </div>
                )}

                {/* Submit button */}
                <button
                  type="submit"
                  disabled={formSubmitting}
                  className="mt-1 w-full flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 disabled:from-blue-300 disabled:to-indigo-300 text-white text-sm font-bold py-3 px-4 shadow-md shadow-blue-200/60 transition-all duration-200 active:scale-[0.98] focus:outline-none focus:ring-4 focus:ring-blue-200"
                >
                  {formSubmitting ? (
                    <>
                      <RefreshCw className="h-4 w-4 animate-spin" />
                      Envoi en cours...
                    </>
                  ) : (
                    <>
                      <Send className="h-4 w-4" />
                      Soumettre la demande
                    </>
                  )}
                </button>
              </form>
            </div>
          </div>
        </div>

        {/* ═══════════════════════════════════════════════════════════════════
            SECTION 2 — History  (3/5 on lg)
        ══════════════════════════════════════════════════════════════════ */}
        <div className="lg:col-span-3 flex flex-col gap-4">

          {/* History header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="p-1.5 bg-indigo-50 text-indigo-600 rounded-xl">
                <Inbox className="h-4 w-4" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-gray-900">Historique des demandes</h2>
                <p className="text-[11px] text-gray-400 font-medium mt-0.5">
                  {requests.length} demande{requests.length !== 1 ? 's' : ''} au total
                </p>
              </div>
            </div>

            {/* Refresh button */}
            <button
              onClick={() => loadHistory()}
              disabled={historyLoading}
              title="Rafraîchir"
              className="p-2 rounded-xl bg-white border border-gray-200 text-gray-400 hover:text-blue-600 hover:border-blue-200 hover:bg-blue-50 transition-all duration-150 shadow-sm disabled:opacity-50"
            >
              <RotateCcw className={`h-4 w-4 ${historyLoading ? 'animate-spin' : ''}`} />
            </button>
          </div>

          {/* Status legend */}
          <div className="flex flex-wrap gap-2">
            {Object.entries(STATUS_CONFIG).map(([key, cfg]) => (
              <StatusBadge key={key} status={key} />
            ))}
          </div>

          {/* Content */}
          {historyLoading ? (
            <div className="flex flex-col items-center justify-center gap-3 bg-white/60 backdrop-blur-sm border border-gray-200/80 rounded-2xl py-16">
              <RefreshCw className="h-7 w-7 text-blue-500 animate-spin" />
              <p className="text-sm text-gray-400 font-medium">
                Chargement de l'historique...
              </p>
            </div>
          ) : historyError ? (
            <div className="flex flex-col items-center justify-center gap-3 bg-red-50 border border-red-200 rounded-2xl py-16 text-center px-6">
              <XCircle className="h-8 w-8 text-red-400" />
              <p className="text-sm text-red-600 font-semibold">{historyError}</p>
              <button
                onClick={() => loadHistory()}
                className="text-xs text-red-500 underline underline-offset-2 font-medium hover:text-red-700 transition-colors"
              >
                Réessayer
              </button>
            </div>
          ) : requests.length === 0 ? (
            <div className="flex flex-col items-center justify-center gap-3 bg-white/60 backdrop-blur-sm border border-dashed border-gray-200 rounded-2xl py-16 text-center px-6">
              <CalendarDays className="h-10 w-10 text-gray-200" />
              <div>
                <p className="text-sm font-semibold text-gray-400">Aucune demande</p>
                <p className="text-xs text-gray-300 font-medium mt-1">
                  Vos futures demandes apparaîtront ici.
                </p>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {requests.map((req, idx) => (
                <LeaveRequestCard key={req.id ?? idx} request={req} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
