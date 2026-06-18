import React, { useState, useMemo, useEffect, useCallback } from 'react';
import { Clock, AlertTriangle } from 'lucide-react';
import useAuthStore from '../store/useAuthStore';
import apiClient from '../api/apiClient';
import timeTrackingApi from '../api/timeTrackingApi';

// Helper to format 24h string "08:00" to "08:00 AM"
const formatTo12Hour = (time24) => {
  if (!time24) return '';
  try {
    const parts = time24.split(':');
    if (parts.length < 2) return time24;
    const hours = parseInt(parts[0], 10);
    const minutes = parts[1];
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    const padHours = displayHours.toString().padStart(2, '0');
    return `${padHours}:${minutes} ${ampm}`;
  } catch (e) {
    return time24;
  }
};

// Generate pay periods (16th of Month N to 15th of Month N+1)
const generatePayPeriods = () => {
  const periods = [];
  const today = new Date();
  let year = today.getFullYear();
  let month = today.getMonth();

  for (let i = 0; i < 5; i++) {
    let startYear = year;
    let startMonth = month - i;

    while (startMonth < 0) {
      startMonth += 12;
      startYear -= 1;
    }

    let endYear = startYear;
    let endMonth = startMonth + 1;
    if (endMonth > 11) {
      endMonth -= 12;
      endYear += 1;
    }

    const startDate = new Date(startYear, startMonth, 16);
    const endDate = new Date(endYear, endMonth, 15);

    // Format months in French
    const startMonthStr = startDate.toLocaleDateString('fr-FR', { month: 'long' });
    const endMonthStr = endDate.toLocaleDateString('fr-FR', { month: 'long' });

    periods.push({
      id: i,
      startDate,
      endDate,
      label: `16 ${startMonthStr} ${startYear} - 15 ${endMonthStr} ${endYear}`
    });
  }
  return periods;
};

export default function TimecardGrid({ employeeId: propEmployeeId, onShowToast }) {
  const payPeriods = useMemo(() => generatePayPeriods(), []);
  const [selectedPeriodId, setSelectedPeriodId] = useState(0);
  const [timesheetData, setTimesheetData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Local state for edits: { [rowIdx]: { in, out, payCode, accrual } }
  const [editState, setEditState] = useState({});

  const activePeriod = payPeriods[selectedPeriodId];

  const { user } = useAuthStore();
  const employeeId = propEmployeeId || user?.profileId || user?.id;
  const userRole = user?.role || user?.roles?.[0] || 'EMPLOYEE';
  const isManager = userRole === 'MANAGER';
  const isHrAdmin = userRole === 'HR_ADMIN' || userRole === 'DIRECTION';

  const loadTimesheet = useCallback(async () => {
    if (!employeeId) return;
    setLoading(true);
    setError(null);
    try {
      // Fetch report using the start date of the period (middle of the month N to N+1)
      const targetDateStr = activePeriod.startDate.toISOString().split('T')[0];
      const report = await timeTrackingApi.getMonthlyReport(employeeId, targetDateStr);
      
      const rows = (report.dailyDetails || []).map((detail) => {
        const dateObj = new Date(detail.date);
        const dayOfWeek = dateObj.getDay();
        const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;

        const dayName = dateObj.toLocaleDateString('fr-FR', { weekday: 'short' });
        const dayNum = dateObj.getDate().toString().padStart(2, '0');
        const monthNum = (dateObj.getMonth() + 1).toString().padStart(2, '0');
        const dateLabel = `${dayName} ${dayNum}/${monthNum}`;

        return {
          date: dateLabel,
          rawDate: detail.date,
          schedule: isWeekend ? '—' : '08:00 AM - 04:00 PM',
          in: detail.checkInTime ? formatTo12Hour(detail.checkInTime) : '',
          out: detail.checkOutTime ? formatTo12Hour(detail.checkOutTime) : '',
          payCode: detail.payCode || '',
          accrual: detail.accrual || '—',
          isMissingPunch: detail.status === 'Absence injustifiée',
          missingType: !detail.checkInTime ? 'in' : (!detail.checkOutTime ? 'out' : ''),
          status: detail.status,
          trackingId: detail.trackingId,
          isWeekend
        };
      });
      setTimesheetData(rows);
    } catch (err) {
      console.error("Failed to load timesheet report:", err);
      setError("Erreur lors de la récupération de la carte de temps.");
    } finally {
      setLoading(false);
    }
  }, [employeeId, activePeriod]);

  useEffect(() => {
    loadTimesheet();
    setEditState({}); // clear edit state when period changes
  }, [loadTimesheet]);

  // Calculations for totals
  const totals = useMemo(() => {
    let workedSeconds = 0;
    let leaveHours = 0;
    let anomalies = 0;

    timesheetData.forEach((row) => {
      if (row.isMissingPunch || row.status === 'Absence injustifiée') {
        anomalies++;
      }
      
      if (row.payCode) {
        const h = parseFloat(row.accrual);
        if (!isNaN(h)) {
          leaveHours += h;
        }
      } else if (row.in && row.out) {
        const h = parseFloat(row.accrual);
        if (!isNaN(h)) {
          workedSeconds += h * 3600;
        } else {
          workedSeconds += 8 * 3600; // default 8 hours standard
        }
      }
    });

    const workedHours = workedSeconds / 3600;
    const cumulHours = workedHours + leaveHours;

    return {
      workedHours: workedHours.toFixed(2),
      leaveHours: leaveHours.toFixed(2),
      cumulHours: cumulHours.toFixed(2),
      anomalies
    };
  }, [timesheetData]);

  const handleCellChange = (idx, field, value) => {
    setEditState((prev) => {
      const rowData = prev[idx] || {
        in: timesheetData[idx].in,
        out: timesheetData[idx].out,
        payCode: timesheetData[idx].payCode,
        accrual: timesheetData[idx].accrual
      };
      return {
        ...prev,
        [idx]: {
          ...rowData,
          [field]: value
        }
      };
    });
  };

  const handleSaveCorrection = async (idx) => {
    const row = timesheetData[idx];
    const edited = editState[idx] || {};

    const requestPayload = {
      employeeId: Number(employeeId),
      date: row.rawDate,
      checkInTime: edited.in !== undefined ? edited.in : row.in,
      checkOutTime: edited.out !== undefined ? edited.out : row.out,
      payCode: edited.payCode !== undefined ? edited.payCode : row.payCode,
      accrual: edited.accrual !== undefined ? edited.accrual : row.accrual
    };

    try {
      await apiClient.put('/api/v1/tracking/correct', requestPayload);
      if (onShowToast) {
        onShowToast("Pointage corrigé avec succès !", "success");
      }
      // Reload timesheet
      loadTimesheet();
      // Remove from edit state
      setEditState((prev) => {
        const next = { ...prev };
        delete next[idx];
        return next;
      });
    } catch (err) {
      console.error("Failed to correct time tracking:", err);
      if (onShowToast) {
        onShowToast("Erreur lors de la correction du pointage.", "error");
      }
    }
  };

  const handleJustifyAbsence = async (trackingId) => {
    if (!trackingId) return;
    try {
      await timeTrackingApi.overrideLatePunchIn(trackingId);
      if (onShowToast) {
        onShowToast("Absence justifiée comme Maladie avec succès !", "success");
      }
      loadTimesheet();
    } catch (err) {
      console.error("Failed to justify absence:", err);
      if (onShowToast) {
        onShowToast("Erreur lors de la justification de l'absence.", "error");
      }
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-3xl shadow-sm overflow-hidden mt-6">
      {/* Header Widget Panel */}
      <div className="px-6 py-4.5 border-b border-gray-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-slate-50/50">
        <div>
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-indigo-650" />
            <h2 className="text-sm font-bold text-gray-900">Grille de Carte de Temps · Timecard Grid</h2>
          </div>
          <p className="text-[11px] text-gray-400 font-medium mt-0.5">
            Période de paie active : {activePeriod.label}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3 self-stretch sm:self-auto justify-end">
          <select
            value={selectedPeriodId}
            onChange={(e) => setSelectedPeriodId(Number(e.target.value))}
            className="text-xs bg-white border border-gray-200 rounded-xl px-3 py-1.5 font-bold text-gray-750 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all shadow-sm"
          >
            {payPeriods.map((period) => (
              <option key={period.id} value={period.id}>
                {period.label}
              </option>
            ))}
          </select>
          {totals.anomalies > 0 && (
            <span className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-1 rounded-lg border border-amber-100 animate-pulse">
              <AlertTriangle className="h-3 w-3 text-amber-500" />
              {totals.anomalies} Anomalies à corriger
            </span>
          )}
          <button className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-750 text-white font-bold text-xs rounded-xl shadow-sm transition-colors active:scale-95">
            Approuver la Carte
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 text-gray-400 gap-2">
          <Clock className="h-6 w-6 text-indigo-650 animate-spin" />
          <span className="text-xs font-semibold">Chargement de la carte de temps...</span>
        </div>
      ) : error ? (
        <div className="py-20 text-center text-rose-500 font-semibold text-xs">{error}</div>
      ) : (
        /* Grid Container */
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left border-collapse min-w-[700px]">
            <thead>
              <tr className="bg-slate-100/80 border-b border-gray-200 text-gray-500 font-bold uppercase tracking-wider text-[10px]">
                <th className="py-2.5 px-3 border-r border-gray-200">Date</th>
                <th className="py-2.5 px-3 border-r border-gray-200">Schedule</th>
                <th className="py-2.5 px-3 border-r border-gray-200">In</th>
                <th className="py-2.5 px-3 border-r border-gray-200">Out</th>
                <th className="py-2.5 px-3 border-r border-gray-200">Pay code</th>
                <th className="py-2.5 px-3 border-r border-gray-200">Accrual</th>
                {(isManager || isHrAdmin) && <th className="py-2.5 px-3">Actions</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {timesheetData.map((row, idx) => {
                const isEven = idx % 2 === 0;
                const bgClass = isEven ? 'bg-white' : 'bg-slate-50';
                
                // Get edited values if they exist
                const editedVal = editState[idx] || {};
                const currentIn = editedVal.in !== undefined ? editedVal.in : row.in;
                const currentOut = editedVal.out !== undefined ? editedVal.out : row.out;
                const currentPayCode = editedVal.payCode !== undefined ? editedVal.payCode : row.payCode;
                const currentAccrual = editedVal.accrual !== undefined ? editedVal.accrual : row.accrual;
                
                // Highlight unsaved edits
                const isRowEdited = editState[idx] !== undefined;

                return (
                  <tr key={row.rawDate} className={`${bgClass} hover:bg-slate-100/50 transition-colors font-mono`}>
                    {/* Date */}
                    <td className="py-2 px-3 border-r border-gray-200 font-sans font-semibold text-gray-700">
                      {row.date}
                    </td>
                    {/* Schedule */}
                    <td className="py-2 px-3 border-r border-gray-200 text-gray-650">
                      {row.schedule}
                    </td>
                    {/* In */}
                    <td className="py-2 px-3 border-r border-gray-200 font-bold text-gray-800">
                      {isManager ? (
                        <input
                          type="text"
                          value={currentIn}
                          onChange={(e) => handleCellChange(idx, 'in', e.target.value)}
                          placeholder="hh:mm AM/PM"
                          className={`w-full bg-slate-50 border border-gray-200 rounded px-1.5 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 font-mono font-bold ${
                            isRowEdited ? 'bg-yellow-50/50 border-yellow-200' : ''
                          }`}
                        />
                      ) : row.isMissingPunch && row.missingType === 'in' ? (
                        <span className="flex items-center gap-1 font-sans text-[10px] font-medium text-rose-600 animate-pulse">
                          <AlertTriangle className="h-3 w-3 shrink-0 text-rose-500" />
                          Manquant
                        </span>
                      ) : (
                        row.in || '—'
                      )}
                    </td>
                    {/* Out */}
                    <td className="py-2 px-3 border-r border-gray-200 font-bold text-gray-800">
                      {isManager ? (
                        <input
                          type="text"
                          value={currentOut}
                          onChange={(e) => handleCellChange(idx, 'out', e.target.value)}
                          placeholder="hh:mm AM/PM"
                          className={`w-full bg-slate-50 border border-gray-200 rounded px-1.5 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 font-mono font-bold ${
                            isRowEdited ? 'bg-yellow-50/50 border-yellow-200' : ''
                          }`}
                        />
                      ) : row.isMissingPunch && row.missingType === 'out' ? (
                        <span className="flex items-center gap-1 font-sans text-[10px] font-medium text-rose-600 animate-pulse">
                          <AlertTriangle className="h-3 w-3 shrink-0 text-rose-500" />
                          Manquant
                        </span>
                      ) : (
                        row.out || '—'
                      )}
                    </td>
                    {/* Pay code */}
                    <td className="py-2 px-3 border-r border-gray-200 font-sans font-semibold">
                      {isManager ? (
                        <select
                          value={currentPayCode}
                          onChange={(e) => handleCellChange(idx, 'payCode', e.target.value)}
                          className={`w-full bg-slate-50 border border-gray-200 rounded px-1.5 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 font-sans font-bold ${
                            isRowEdited ? 'bg-yellow-50/50 border-yellow-200' : ''
                          }`}
                        >
                          <option value="">—</option>
                          <option value="C - Congés Payés">C - Congés Payés</option>
                          <option value="M - Maladie">M - Maladie</option>
                        </select>
                      ) : row.payCode ? (
                        <span className="text-purple-700 bg-purple-50 border border-purple-100 rounded px-1.5 py-0.5 text-[10px] inline-block shadow-sm">
                          {row.payCode}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    {/* Accrual */}
                    <td className="py-2 px-3 text-gray-900 font-bold border-r border-gray-200">
                      {isManager ? (
                        <input
                          type="text"
                          value={currentAccrual}
                          onChange={(e) => handleCellChange(idx, 'accrual', e.target.value)}
                          placeholder="8.00"
                          className={`w-full bg-slate-50 border border-gray-200 rounded px-1.5 py-0.5 text-xs focus:outline-none focus:ring-1 focus:ring-indigo-500 font-mono font-bold ${
                            isRowEdited ? 'bg-yellow-50/50 border-yellow-200' : ''
                          }`}
                        />
                      ) : (
                        row.accrual
                      )}
                    </td>
                    {/* Actions */}
                    {(isManager || isHrAdmin) && (
                      <td className="py-2 px-3">
                        <div className="flex gap-2">
                          {isManager && (
                            <button
                              onClick={() => handleSaveCorrection(idx)}
                              disabled={!isRowEdited}
                              className={`px-2.5 py-1 font-bold text-[10px] rounded-lg shadow-sm transition-all ${
                                isRowEdited
                                  ? 'bg-indigo-600 hover:bg-indigo-750 text-white active:scale-95'
                                  : 'bg-gray-100 text-gray-400 cursor-not-allowed shadow-none'
                              }`}
                            >
                              Enregistrer
                            </button>
                          )}
                          {isHrAdmin && row.status === 'Absence injustifiée' && row.trackingId && (
                            <button
                              onClick={() => handleJustifyAbsence(row.trackingId)}
                              className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-[10px] rounded-lg shadow-sm transition-all active:scale-95"
                            >
                              Justifier (Maladie)
                            </button>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>

            {/* Table Footer */}
            <tfoot>
              <tr className="bg-slate-100 border-t border-gray-300 font-bold text-gray-800">
                <td colSpan="3" className="py-3 px-4 border-r border-gray-200 font-sans text-xs">
                  <div className="flex flex-col gap-1">
                    <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">Accruals (Droits & Soldes)</span>
                    <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-slate-700">
                      <span className="flex items-center gap-1">
                        <span className="h-1.5 w-1.5 rounded-full bg-purple-500" />
                        Congés Payés (CP): <strong className="text-gray-900">22.5 j</strong>
                      </span>
                      <span className="flex items-center gap-1">
                        <span className="h-1.5 w-1.5 rounded-full bg-blue-500" />
                        RTT Restants: <strong className="text-gray-900">7.0 j</strong>
                      </span>
                    </div>
                  </div>
                </td>
                <td colSpan={(isManager || isHrAdmin) ? 4 : 3} className="py-3 px-4 font-sans text-xs bg-indigo-50/30">
                  <div className="flex flex-col gap-1">
                    <span className="text-[10px] text-indigo-500 font-bold uppercase tracking-wider">Totals (Heures Période)</span>
                    <div className="grid grid-cols-3 gap-2 text-indigo-950 font-semibold">
                      <div>
                        <span className="text-[10px] text-gray-400 block font-normal">Total Travaillé</span>
                        <span className="text-xs font-extrabold text-gray-800">{totals.workedHours} h</span>
                      </div>
                      <div>
                        <span className="text-[10px] text-gray-400 block font-normal">Total Congé</span>
                        <span className="text-xs font-extrabold text-purple-700">{totals.leaveHours} h</span>
                      </div>
                      <div>
                        <span className="text-[10px] text-indigo-400 block font-normal">Cumul Période</span>
                        <span className="text-sm font-black text-indigo-700">{totals.cumulHours} h</span>
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      )}
    </div>
  );
}
