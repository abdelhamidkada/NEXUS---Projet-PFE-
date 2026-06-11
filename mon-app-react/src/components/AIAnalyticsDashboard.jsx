import React, { useState, useEffect } from 'react';
import {
  TrendingUp,
  AlertTriangle,
  UserCheck,
  GraduationCap,
  BrainCircuit,
  Info,
  Calendar,
  ChevronRight,
  Loader2
} from 'lucide-react';
import apiClient from '../api/apiClient';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';

// ---------------------------------------------------------------------------
// Mock Data - Populated for Premium Visuals
// ---------------------------------------------------------------------------

const mockAbsenceData = [
  { month: 'Jan', Réel: 4.2, Prédiction: 4.5 },
  { month: 'Fév', Réel: 3.8, Prédiction: 4.0 },
  { month: 'Mar', Réel: 3.5, Prédiction: 3.8 },
  { month: 'Avr', Réel: 4.8, Prédiction: 4.5 },
  { month: 'Mai', Réel: 5.2, Prédiction: 5.0 },
  { month: 'Jui', Réel: 3.1, Prédiction: 3.3 },
  { month: 'Jul', Réel: 2.8, Prédiction: 3.0 },
  { month: 'Aoû', Réel: 2.5, Prédiction: 2.7 },
  { month: 'Sep', Réel: 4.1, Prédiction: 4.3 },
  { month: 'Oct', Réel: 4.5, Prédiction: 4.6 },
  { month: 'Nov', Réel: 5.0, Prédiction: 5.2 },
  { month: 'Déc', Réel: 5.5, Prédiction: 5.8 }
];

const mockTrainingData = [
  { name: 'Technique', value: 45, color: '#3b82f6' },
  { name: 'Management / RH', value: 25, color: '#8b5cf6' },
  { name: 'Marketing / Design', value: 30, color: '#10b981' }
];

const mockAttritionRisk = [
  { id: 1, name: 'Antoine Lefebvre', role: 'Administrateur Systèmes', department: 'Pôle IT & Support', factor: 'Charge de travail & Astreintes', riskScore: 82, status: 'Critique' },
  { id: 2, name: 'Camille Roux', role: 'Ingénieure DevOps', department: 'Pôle IT & Support', factor: 'Opportunités de formation', riskScore: 74, status: 'Élevé' },
  { id: 3, name: 'Lucas Blanc', role: 'Data Analyst', department: 'Pôle IT & Support', factor: 'Répétitivité des tâches', riskScore: 68, status: 'Moyen' }
];

// ---------------------------------------------------------------------------
// Main Component
// ---------------------------------------------------------------------------

export default function AIAnalyticsDashboard() {
  const [kpiData, setKpiData] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchKpis = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const res = await apiClient.get('/api/v1/analytics/kpis');
        setKpiData(res.data);
      } catch (err) {
        console.error("Erreur lors de la récupération des KPIs analytiques:", err);
        setError("Impossible de charger les indicateurs analytiques.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchKpis();
  }, []);

  if (isLoading) {
    return (
      <div className="w-full min-h-[75vh] flex flex-col items-center justify-center text-gray-400">
        <div className="relative flex items-center justify-center mb-4">
          <div className="absolute h-12 w-12 border-2 border-indigo-600/30 rounded-full animate-ping" />
          <Loader2 className="h-8 w-8 text-indigo-600 animate-spin" />
        </div>
        <p className="text-sm font-semibold text-gray-600 tracking-wide animate-pulse">
          Calcul des modèles prédictifs et KPIs...
        </p>
      </div>
    );
  }

  if (error || !kpiData) {
    return (
      <div className="w-full min-h-[75vh] flex flex-col items-center justify-center text-center p-6 bg-white border border-gray-200 rounded-3xl shadow-sm max-w-lg mx-auto mt-12">
        <AlertTriangle className="h-12 w-12 text-rose-500 mb-4" />
        <h3 className="text-base font-bold text-gray-800">Erreur d'analyse</h3>
        <p className="text-xs text-gray-500 mt-2">{error || "Erreur de connexion avec le serveur analytique."}</p>
        <button
          onClick={() => window.location.reload()}
          className="mt-6 px-4.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs rounded-xl shadow transition-colors"
        >
          Réessayer
        </button>
      </div>
    );
  }

  return (
    <div className="w-full min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/20 to-indigo-50/30 p-4 md:p-6 lg:p-8">
      
      {/* Header */}
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1.5">
            <div className="p-2.5 bg-indigo-600 text-white rounded-2xl shadow-md shadow-indigo-200">
              <BrainCircuit className="h-5 w-5 animate-pulse" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold text-gray-900 tracking-tight">
                Tableau de Bord Analytique & IA Prédictive
              </h1>
              <p className="text-xs text-gray-400 font-medium mt-0.5">
                Module d'Aide à la Décision (OAD) · Analyse RH Avancée
              </p>
            </div>
          </div>
        </div>

        {/* AI Status Badge */}
        <div>
          <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200 shadow-sm">
            <span className="h-2 w-2 rounded-full bg-emerald-500 animate-ping" />
            Analyses Prédictives Actives
          </span>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-8">
        {/* KPI 1: Attrition Risk */}
        <div className="bg-white border border-gray-200 rounded-3xl p-5 flex items-start gap-4 shadow-sm hover:shadow-md transition-all duration-200">
          <div className="p-3 rounded-2xl bg-amber-50 text-amber-600 shrink-0">
            <AlertTriangle className="h-5 w-5" />
          </div>
          <div>
            <p className="text-2xl font-extrabold text-gray-900 leading-none">
              {kpiData.globalAttritionRisk.toFixed(1)}%
            </p>
            <p className="text-xs font-bold text-gray-700 mt-2">Risque d'Attrition Global</p>
            <p className="text-[11px] text-gray-400 mt-0.5">Moyenne pondérée calculée par l'OAD</p>
          </div>
        </div>

        {/* KPI 2: Attendance Rate */}
        <div className="bg-white border border-gray-200 rounded-3xl p-5 flex items-start gap-4 shadow-sm hover:shadow-md transition-all duration-200">
          <div className="p-3 rounded-2xl bg-emerald-50 text-emerald-600 shrink-0">
            <UserCheck className="h-5 w-5" />
          </div>
          <div>
            <p className="text-2xl font-extrabold text-gray-900 leading-none">
              {kpiData.averageAttendanceRate.toFixed(1)}%
            </p>
            <p className="text-xs font-bold text-gray-700 mt-2">Taux de Présence Moyen</p>
            <p className="text-[11px] text-gray-400 mt-0.5">Présences actives ce jour</p>
          </div>
        </div>

        {/* KPI 3: Training alerts */}
        <div className="bg-white border border-gray-200 rounded-3xl p-5 flex items-start gap-4 shadow-sm hover:shadow-md transition-all duration-200">
          <div className="p-3 rounded-2xl bg-blue-50 text-blue-600 shrink-0">
            <GraduationCap className="h-5 w-5" />
          </div>
          <div>
            <p className="text-2xl font-extrabold text-gray-900 leading-none">
              {kpiData.trainingAlertsCount}
            </p>
            <p className="text-xs font-bold text-gray-700 mt-2">Alertes Formation</p>
            <p className="text-[11px] text-gray-400 mt-0.5">Niveau sous le seuil critique (&le; 2/5)</p>
          </div>
        </div>
      </div>

      {/* Grid Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 xl:gap-8 mb-8">
        
        {/* LSTM Absence Forecast Chart */}
        <div className="bg-white border border-gray-200 rounded-3xl p-6 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-4 border-b border-gray-100 pb-3">
            <div>
              <h2 className="text-sm font-bold text-gray-955">Prévision des pics d'absences</h2>
              <p className="text-[10px] text-gray-400 font-semibold uppercase tracking-wider mt-0.5">
                Données d'absentéisme projetées sur 12 mois
              </p>
            </div>
            <Info className="h-4 w-4 text-gray-300" title="Modèle prédictif d'absentéisme" />
          </div>
          <div className="h-64 w-full text-xs">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={mockAbsenceData} margin={{ top: 10, right: 10, left: -25, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorReel" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.2}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorPred" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.2}/>
                    <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="month" tickLine={false} axisLine={false} />
                <YAxis tickLine={false} axisLine={false} />
                <Tooltip />
                <Legend iconType="circle" />
                <Area name="Réel (Observé)" type="monotone" dataKey="Réel" stroke="#3b82f6" strokeWidth={2.5} fillOpacity={1} fill="url(#colorReel)" />
                <Area name="Projection prédictive" type="monotone" dataKey="Prédiction" stroke="#8b5cf6" strokeWidth={2} strokeDasharray="4 4" fillOpacity={1} fill="url(#colorPred)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* K-Means Training Clusters Chart */}
        <div className="bg-white border border-gray-200 rounded-3xl p-6 shadow-sm flex flex-col">
          <div className="flex items-center justify-between mb-4 border-b border-gray-100 pb-3">
            <div>
              <h2 className="text-sm font-bold text-gray-955">Besoins en formation identifiés</h2>
              <p className="text-[10px] text-gray-400 font-semibold uppercase tracking-wider mt-0.5">
                Besoins identifiés par segmentation algorithmique
              </p>
            </div>
            <Info className="h-4 w-4 text-gray-300" title="Modèle d'analyse des besoins de formation" />
          </div>
          <div className="h-64 w-full text-xs flex flex-col md:flex-row items-center justify-center gap-6">
            <div className="w-full md:w-1/2 h-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={mockTrainingData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {mockTrainingData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => `${value}%`} />
                </PieChart>
              </ResponsiveContainer>
            </div>

            {/* Custom Legend for PieChart */}
            <div className="flex flex-col gap-3 w-full md:w-1/2">
              {mockTrainingData.map((item) => (
                <div key={item.name} className="flex items-center justify-between p-2 rounded-xl bg-slate-50/50 border border-slate-100/50">
                  <div className="flex items-center gap-2">
                    <span className="h-3 w-3 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                    <span className="font-semibold text-gray-700 text-xs">{item.name}</span>
                  </div>
                  <span className="font-bold text-gray-900 text-xs">{item.value}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>

      </div>

      {/* Random Forest Attrition Risk Table */}
      <div className="bg-white border border-gray-200 rounded-3xl shadow-sm overflow-hidden">
        <div className="px-6 py-5 border-b border-gray-100 flex items-center justify-between">
          <div>
            <h2 className="text-sm font-bold text-gray-900">Risques de départs identifiés</h2>
            <p className="text-[11px] text-gray-400 font-medium mt-0.5">
              Prédiction d'attrition basée sur les facteurs comportementaux et contractuels
            </p>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100">
                <th className="py-3 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">Collaborateur</th>
                <th className="py-3 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">Département</th>
                <th className="py-3 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">Facteur principal</th>
                <th className="py-3 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">Score de Risque</th>
                <th className="py-3 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-right">Statut</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {mockAttritionRisk.map((emp) => {
                const isCritique = emp.riskScore >= 80;
                const isHigh = emp.riskScore >= 70 && emp.riskScore < 80;

                let scoreColor = 'text-amber-600 bg-amber-50 border-amber-100';
                if (isCritique) scoreColor = 'text-red-600 bg-red-50 border-red-100';
                else if (isHigh) scoreColor = 'text-orange-600 bg-orange-50 border-orange-100';

                return (
                  <tr key={emp.id} className="hover:bg-slate-50/40 transition-colors">
                    {/* Collaborateur */}
                    <td className="py-4 px-6">
                      <div>
                        <p className="font-bold text-gray-900 text-xs">{emp.name}</p>
                        <p className="text-[10px] text-gray-400 font-medium">{emp.role}</p>
                      </div>
                    </td>

                    {/* Département */}
                    <td className="py-4 px-6">
                      <span className="inline-flex items-center text-xs font-semibold text-gray-500 bg-gray-100 px-2 py-0.5 rounded">
                        {emp.department}
                      </span>
                    </td>

                    {/* Facteur Principal */}
                    <td className="py-4 px-6">
                      <p className="text-xs text-gray-600 font-medium italic">{emp.factor}</p>
                    </td>

                    {/* Score de Risque */}
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-2">
                        <div className="w-16 bg-gray-100 rounded-full h-1.5 overflow-hidden">
                          <div
                            className={`h-full ${isCritique ? 'bg-red-500' : isHigh ? 'bg-orange-400' : 'bg-amber-400'}`}
                            style={{ width: `${emp.riskScore}%` }}
                          />
                        </div>
                        <span className="text-xs font-bold text-gray-900">{emp.riskScore}%</span>
                      </div>
                    </td>

                    {/* Statut */}
                    <td className="py-4 px-6 text-right">
                      <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold border ${scoreColor}`}>
                        <span className={`h-1.5 w-1.5 rounded-full shrink-0 ${isCritique ? 'bg-red-500 animate-pulse' : 'bg-orange-500'}`} />
                        {emp.status}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
      
    </div>
  );
}
