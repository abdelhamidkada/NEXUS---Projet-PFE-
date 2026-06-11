import React from 'react';
import {
  Award,
  BookOpen,
  CheckCircle,
  Plus,
  TrendingUp,
  Brain,
  Sliders,
  Sparkles,
  Users
} from 'lucide-react';

// ---------------------------------------------------------------------------
// Mock Data (Hard & Soft Skills, Certifications for HR Profile)
// ---------------------------------------------------------------------------
const MOCK_HR_HARD_SKILLS = [
  { name: 'Recrutement & Sourcing', percentage: 90, level: 'Expert', color: 'bg-blue-600' },
  { name: 'Droit du Travail', percentage: 85, level: 'Expert', color: 'bg-indigo-600' },
  { name: 'GPEC (Gestion Prévisionnelle des Emplois et Compétences)', percentage: 80, level: 'Avancé', color: 'bg-violet-600' },
  { name: 'Gestion de la Paie', percentage: 70, level: 'Avancé', color: 'bg-emerald-600' },
];

const MOCK_HR_SOFT_SKILLS = [
  { name: 'Gestion des conflits', color: 'bg-blue-50 text-blue-700 border-blue-100' },
  { name: 'Communication interpersonnelle', color: 'bg-indigo-50 text-indigo-700 border-indigo-100' },
  { name: 'Leadership & Management d\'équipe', color: 'bg-violet-50 text-violet-700 border-violet-100' },
  { name: 'Écoute active & Empathie', color: 'bg-emerald-50 text-emerald-700 border-emerald-100' },
];

const MOCK_HR_CERTIFICATIONS = [
  { title: 'Certification Expert SIRH', issuer: 'NEXUS Academy', date: '2025' },
  { title: 'Mise à jour Droit Social 2026', issuer: 'Institut National du Travail', date: '2026' },
  { title: 'Certification GPEC & Stratégie RH', issuer: 'HR Certification Institute', date: '2024' },
];

// Helper functions for dynamic skills mapping
const getSkillColor = (category) => {
  const cat = (category || '').toLowerCase();
  if (cat.includes('it') || cat.includes('data') || cat.includes('technique')) return 'bg-blue-600';
  if (cat.includes('design') || cat.includes('produit')) return 'bg-indigo-600';
  if (cat.includes('management') || cat.includes('rh') || cat.includes('ressources')) return 'bg-violet-600';
  return 'bg-emerald-600';
};

const getSkillLevelName = (level) => {
  switch (level) {
    case 1: return 'Débutant';
    case 2: return 'Intermédiaire';
    case 3: return 'Avancé';
    case 4: return 'Très Avancé';
    case 5: return 'Expert';
    default: return 'Avancé';
  }
};

// ---------------------------------------------------------------------------
// Main Component
// ---------------------------------------------------------------------------
export default function SkillsMatrixView({ userProfile, onShowToast }) {
  const handleAction = (type) => {
    if (onShowToast) {
      onShowToast(
        type === 'eval'
          ? "Demande d'évaluation envoyée à la direction."
          : "Ouverture du formulaire d'ajout de compétence. (Simulation)",
        'success'
      );
    } else {
      console.log(`Action: ${type}`);
    }
  };

  const skills = userProfile?.skills || [];

  return (
    <div className="w-full min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/20 to-indigo-50/30 p-4 md:p-6 lg:p-8">
      
      {/* Header */}
      <div className="mb-8 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1.5">
            <div className="p-2.5 bg-blue-600 text-white rounded-2xl shadow-md shadow-blue-200">
              <Award className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold text-gray-900 tracking-tight">
                Matrice des Compétences & Évaluations - {userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : 'Mon Profil'}
              </h1>
              <p className="text-xs text-gray-400 font-medium mt-0.5">
                {userProfile 
                  ? `${userProfile.jobTitle} · ${userProfile.department}` 
                  : 'Cartographie des compétences métiers et plan de développement professionnel'}
              </p>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => handleAction('eval')}
            className="inline-flex items-center gap-1.5 px-4 py-2 border border-gray-200 bg-white hover:bg-gray-50 text-gray-700 font-bold text-xs rounded-xl shadow-sm transition-all active:scale-95"
          >
            <Sliders className="h-3.5 w-3.5" />
            Demander une évaluation
          </button>
          
          <button
            onClick={() => handleAction('add')}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-xl shadow-md shadow-blue-200 transition-all active:scale-95"
          >
            <Plus className="h-3.5 w-3.5" />
            Ajouter une compétence
          </button>
        </div>
      </div>

      {/* Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 xl:gap-8">
        
        {/* Section 1: Hard Skills */}
        <div className="bg-white/80 backdrop-blur-md border border-gray-200/80 rounded-3xl p-6 shadow-sm flex flex-col">
          <div className="flex items-center gap-2.5 mb-6 border-b border-gray-100 pb-4">
            <div className="p-1.5 bg-blue-50 text-blue-600 rounded-xl">
              <Sliders className="h-4 w-4" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-gray-900">Compétences Professionnelles</h2>
              <p className="text-[11px] text-gray-400 font-medium mt-0.5">
                Compétences techniques et expertise métier issues de votre profil
              </p>
            </div>
          </div>

          {/* Hard Skills List */}
          <div className="space-y-5 flex-1">
            {skills.length > 0 ? (
              Array.from(skills).map((skill) => {
                const percentage = (skill.proficiencyLevel || 3) * 20;
                const levelName = getSkillLevelName(skill.proficiencyLevel);
                const color = getSkillColor(skill.category);
                return (
                  <div key={skill.name} className="flex flex-col gap-2">
                    <div className="flex items-center justify-between text-xs font-bold text-gray-700">
                      <span>{skill.name}</span>
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-bold text-blue-600 bg-blue-50 border border-blue-100 px-1.5 py-0.5 rounded">
                          {skill.category}
                        </span>
                        <span className="text-[10px] text-gray-400 font-semibold">{levelName} ({skill.proficiencyLevel}/5)</span>
                      </div>
                    </div>
                    
                    {/* Progress bar */}
                    <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden shadow-inner">
                      <div
                        className={`h-full rounded-full transition-all duration-500 ${color}`}
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  </div>
                );
              })
            ) : (
              <p className="text-xs text-gray-400 italic text-center py-8">
                Aucune compétence répertoriée dans votre profil.
              </p>
            )}
          </div>
        </div>

        {/* Section 2: Soft Skills & Certifications */}
        <div className="flex flex-col gap-6 xl:gap-8">
          
          {/* Soft Skills Card */}
          <div className="bg-white/80 backdrop-blur-md border border-gray-200/80 rounded-3xl p-6 shadow-sm">
            <div className="flex items-center gap-2.5 mb-5 border-b border-gray-100 pb-3">
              <div className="p-1.5 bg-indigo-50 text-indigo-600 rounded-xl">
                <Brain className="h-4 w-4" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-gray-900">Soft Skills</h2>
                <p className="text-[11px] text-gray-400 font-medium mt-0.5">
                  Qualités comportementales et relationnelles appréciées
                </p>
              </div>
            </div>

            {/* Soft Skills Badges */}
            <div className="flex flex-wrap gap-2.5">
              {MOCK_HR_SOFT_SKILLS.map((soft) => (
                <span
                  key={soft.name}
                  className={`inline-flex items-center gap-1 px-3 py-1.5 rounded-2xl text-xs font-bold border shadow-sm ${soft.color}`}
                >
                  <Sparkles className="h-3 w-3 shrink-0" />
                  {soft.name}
                </span>
              ))}
            </div>
          </div>

          {/* Certifications Card */}
          <div className="bg-white/80 backdrop-blur-md border border-gray-200/80 rounded-3xl p-6 shadow-sm flex-1">
            <div className="flex items-center gap-2.5 mb-5 border-b border-gray-100 pb-3">
              <div className="p-1.5 bg-emerald-50 text-emerald-600 rounded-xl">
                <BookOpen className="h-4 w-4" />
              </div>
              <div>
                <h2 className="text-sm font-bold text-gray-900">Certifications Actives</h2>
                <p className="text-[11px] text-gray-400 font-medium mt-0.5">
                  Titres et validations professionnelles certifiés
                </p>
              </div>
            </div>

            {/* Certifications list */}
            <div className="space-y-3">
              {MOCK_HR_CERTIFICATIONS.map((cert) => (
                <div
                  key={cert.title}
                  className="flex items-start gap-3 p-3 bg-slate-50/50 hover:bg-slate-50 border border-slate-100 hover:border-slate-200 rounded-2xl transition-all duration-150 shadow-sm"
                >
                  <div className="p-2 bg-emerald-50 text-emerald-600 rounded-xl shadow-inner shrink-0">
                    <CheckCircle className="h-4 w-4" />
                  </div>
                  <div>
                    <h4 className="text-xs font-bold text-gray-900">{cert.title}</h4>
                    <p className="text-[10px] text-gray-400 font-semibold mt-0.5">
                      {cert.issuer} · Obtenue en {cert.date}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>

        </div>

      </div>

    </div>
  );
}
