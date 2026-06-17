import React from 'react';
import { Clock, AlertTriangle, Calendar, Award, CheckCircle2 } from 'lucide-react';

const mockTimesheetData = [
  {
    date: 'Lun 15/06',
    schedule: '09:00 AM - 05:00 PM',
    absence: '',
    in: '09:00 AM',
    transfer: '',
    out: '05:00 PM',
    payCode: '',
    amount: '',
    shift: '8.00',
    daily: '8.00',
    period: '8.00',
    isMissingPunch: false,
  },
  {
    date: 'Mar 16/06',
    schedule: '01:00 PM - 05:00 PM',
    absence: '',
    in: '01:00 PM',
    transfer: '',
    out: '05:00 PM',
    payCode: '',
    amount: '',
    shift: '4.00',
    daily: '4.00',
    period: '12.00',
    isMissingPunch: false,
  },
  {
    date: 'Mer 17/06',
    schedule: '09:00 AM - 05:00 PM',
    absence: '',
    in: '09:00 AM',
    transfer: 'Pôle IT & Support',
    out: '', // Missing Punch
    payCode: '',
    amount: '',
    shift: '—',
    daily: '—',
    period: '12.00',
    isMissingPunch: true,
    missingType: 'out',
  },
  {
    date: 'Jeu 18/06',
    schedule: '—',
    absence: 'C - Congés Payés',
    in: '',
    transfer: '',
    out: '',
    payCode: 'C - Congés Payés',
    amount: '8.00',
    shift: '',
    daily: '8.00',
    period: '20.00',
    isMissingPunch: false,
    isPayCodeRow: true,
  },
  {
    date: 'Ven 19/06',
    schedule: '09:00 AM - 05:00 PM',
    absence: '',
    in: '', // Missing Punch
    transfer: '',
    out: '05:00 PM',
    payCode: '',
    amount: '',
    shift: '—',
    daily: '—',
    period: '20.00',
    isMissingPunch: true,
    missingType: 'in',
  }
];

export default function TimecardGrid() {
  return (
    <div className="bg-white border border-gray-200 rounded-3xl shadow-sm overflow-hidden mt-6">
      {/* Header Widget Panel */}
      <div className="px-6 py-4.5 border-b border-gray-100 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 bg-slate-50/50">
        <div>
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-indigo-600" />
            <h2 className="text-sm font-bold text-gray-900">Grille de Carte de Temps · Timecard Grid</h2>
          </div>
          <p className="text-[11px] text-gray-400 font-medium mt-0.5">
            Période de paie active : 15 Juin 2026 - 19 Juin 2026 (Hebdomadaire)
          </p>
        </div>
        <div className="flex items-center gap-2 self-stretch sm:self-auto justify-end">
          <span className="inline-flex items-center gap-1 text-[10px] font-bold text-amber-700 bg-amber-50 px-2 py-1 rounded-lg border border-amber-100">
            <AlertTriangle className="h-3 w-3 text-amber-500" />
            2 Anomalies à corriger
          </span>
          <button className="px-3 py-1.5 bg-indigo-650 hover:bg-indigo-700 text-white font-bold text-xs rounded-xl shadow-sm transition-colors">
            Approuver la Carte
          </button>
        </div>
      </div>

      {/* Grid Container */}
      <div className="overflow-x-auto">
        <table className="w-full text-xs text-left border-collapse min-w-[900px]">
          <thead>
            <tr className="bg-slate-100/80 border-b border-gray-200 text-gray-500 font-bold uppercase tracking-wider text-[10px]">
              <th className="py-2.5 px-3 border-r border-gray-200">Date</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Schedule</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Absence</th>
              <th className="py-2.5 px-3 border-r border-gray-200">In</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Transfer</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Out</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Pay code</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Amount</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Shift</th>
              <th className="py-2.5 px-3 border-r border-gray-200">Daily</th>
              <th className="py-2.5 px-3">Period</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {mockTimesheetData.map((row, idx) => {
              const isEven = idx % 2 === 0;
              const bgClass = isEven ? 'bg-white' : 'bg-slate-50';

              return (
                <tr key={row.date} className={`${bgClass} hover:bg-slate-100/50 transition-colors font-mono`}>
                  {/* Date */}
                  <td className="py-2 px-3 border-r border-gray-200 font-sans font-semibold text-gray-700">
                    {row.date}
                  </td>
                  {/* Schedule */}
                  <td className="py-2 px-3 border-r border-gray-200 text-gray-650">
                    {row.schedule}
                  </td>
                  {/* Absence */}
                  <td className="py-2 px-3 border-r border-gray-200 font-sans">
                    {row.absence && (
                      <span className="inline-flex items-center text-[10px] font-semibold text-purple-700 bg-purple-50 px-1.5 py-0.5 rounded border border-purple-100">
                        {row.absence}
                      </span>
                    )}
                  </td>
                  {/* In */}
                  <td className={`py-2 px-3 border-r border-gray-200 font-bold ${
                    row.isMissingPunch && row.missingType === 'in'
                      ? 'bg-rose-50 text-rose-700 border-l-2 border-l-rose-500'
                      : 'text-gray-800'
                  }`}>
                    {row.isMissingPunch && row.missingType === 'in' ? (
                      <span className="flex items-center gap-1 font-sans text-[10px] font-medium text-rose-600">
                        <AlertTriangle className="h-3 w-3 shrink-0 text-rose-500" />
                        Manquant
                      </span>
                    ) : (
                      row.in || '—'
                    )}
                  </td>
                  {/* Transfer */}
                  <td className="py-2 px-3 border-r border-gray-200 text-gray-500 font-sans truncate max-w-[120px]">
                    {row.transfer || '—'}
                  </td>
                  {/* Out */}
                  <td className={`py-2 px-3 border-r border-gray-200 font-bold ${
                    row.isMissingPunch && row.missingType === 'out'
                      ? 'bg-rose-50 text-rose-700 border-l-2 border-l-rose-500'
                      : 'text-gray-800'
                  }`}>
                    {row.isMissingPunch && row.missingType === 'out' ? (
                      <span className="flex items-center gap-1 font-sans text-[10px] font-medium text-rose-600">
                        <AlertTriangle className="h-3 w-3 shrink-0 text-rose-500" />
                        Manquant
                      </span>
                    ) : (
                      row.out || '—'
                    )}
                  </td>
                  {/* Pay code */}
                  <td className="py-2 px-3 border-r border-gray-200 font-sans font-semibold">
                    {row.isPayCodeRow ? (
                      <span className="text-purple-700 bg-purple-50 border border-purple-150 rounded px-1.5 py-0.5 text-[10px] inline-block">
                        {row.payCode}
                      </span>
                    ) : (
                      row.payCode || '—'
                    )}
                  </td>
                  {/* Amount */}
                  <td className="py-2 px-3 border-r border-gray-200 text-gray-750 font-semibold">
                    {row.amount || '—'}
                  </td>
                  {/* Shift */}
                  <td className="py-2 px-3 border-r border-gray-200 text-gray-750 font-semibold">
                    {row.shift || '—'}
                  </td>
                  {/* Daily */}
                  <td className="py-2 px-3 border-r border-gray-200 text-gray-900 font-bold">
                    {row.daily || '—'}
                  </td>
                  {/* Period */}
                  <td className="py-2 px-3 text-indigo-900 font-extrabold bg-indigo-50/20">
                    {row.period || '—'}
                  </td>
                </tr>
              );
            })}
          </tbody>

          {/* Table Footer */}
          <tfoot>
            <tr className="bg-slate-100 border-t border-gray-300 font-bold text-gray-800">
              <td colSpan="6" className="py-3 px-4 border-r border-gray-200 font-sans text-xs">
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
                    <span className="flex items-center gap-1">
                      <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                      Récupérations (Heures): <strong className="text-gray-900">4.5 h</strong>
                    </span>
                  </div>
                </div>
              </td>
              <td colSpan="5" className="py-3 px-4 font-sans text-xs bg-indigo-50/30">
                <div className="flex flex-col gap-1">
                  <span className="text-[10px] text-indigo-500 font-bold uppercase tracking-wider">Totals (Heures Période)</span>
                  <div className="grid grid-cols-3 gap-2 text-indigo-950 font-semibold">
                    <div>
                      <span className="text-[10px] text-gray-400 block font-normal">Total Travaillé</span>
                      <span className="text-xs font-extrabold text-gray-800">12.00 h</span>
                    </div>
                    <div>
                      <span className="text-[10px] text-gray-400 block font-normal">Total Congé</span>
                      <span className="text-xs font-extrabold text-purple-700">8.00 h</span>
                    </div>
                    <div>
                      <span className="text-[10px] text-indigo-400 block font-normal">Cumul Période</span>
                      <span className="text-sm font-black text-indigo-700">20.00 h</span>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
}
