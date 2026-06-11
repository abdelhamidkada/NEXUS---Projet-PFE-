import React, { useState, useEffect } from 'react';
import {
  ArrowLeft,
  Lock,
  Mail,
  User,
  Clock,
  FileText,
  Award,
  AlertCircle,
  CheckCircle,
  Calendar,
  Building2,
  Phone,
  MapPin,
  Signature,
  Globe,
  Briefcase,
  Laptop,
  TrendingUp,
  CreditCard,
  Timer,
  Network,
  ShieldCheck,
  BadgeCheck
} from 'lucide-react';
import apiClient from '../api/apiClient';
import useAuthStore from '../store/useAuthStore';

const LEAVE_TYPES = {
  ANNUAL: { label: 'Congé Annuel', bg: 'bg-blue-50 text-blue-700 border-blue-100' },
  SICK: { label: 'Maladie', bg: 'bg-rose-50 text-rose-700 border-rose-100' },
  UNPAID: { label: 'Sans Solde', bg: 'bg-slate-50 text-slate-700 border-slate-100' },
  MATERNITY: { label: 'Maternité', bg: 'bg-violet-50 text-violet-700 border-violet-100' },
  PATERNITY: { label: 'Paternité', bg: 'bg-indigo-50 text-indigo-700 border-indigo-100' },
  EXCEPTIONAL: { label: 'Congé Exceptionnel', bg: 'bg-amber-50 text-amber-700 border-amber-100' },
};

const WORK_MODEL_LABELS = {
  WFH: { label: 'Télétravail', color: 'bg-emerald-50 text-emerald-700 border-emerald-100' },
  OFFICE: { label: 'Présentiel', color: 'bg-blue-50 text-blue-700 border-blue-100' },
  HYBRID: { label: 'Hybride', color: 'bg-violet-50 text-violet-700 border-violet-100' },
};

const SENIORITY_LABELS = {
  JUNIOR: { label: 'Junior', color: 'bg-sky-50 text-sky-700 border-sky-100' },
  MID: { label: 'Confirmé', color: 'bg-amber-50 text-amber-700 border-amber-100' },
  SENIOR: { label: 'Senior', color: 'bg-orange-50 text-orange-700 border-orange-100' },
  LEAD: { label: 'Lead / Expert', color: 'bg-violet-50 text-violet-700 border-violet-100' },
};

const FRACTION_LABELS = {
  FULL_TIME: 'Temps plein',
  PART_TIME: 'Temps partiel',
};

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

// ── Composant réutilisable pour une carte d'info ──────────────────────────────
const InfoCard = ({ icon: Icon, label, value, locked = false, fullWidth = false }) => (
  <div className={`bg-slate-50 border border-slate-100 rounded-2xl p-4 ${fullWidth ? 'col-span-1 md:col-span-2' : ''}`}>
    <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider flex items-center gap-1.5">
      {Icon && <Icon className="h-3 w-3" />}
      {label}
    </p>
    <div className="text-xs font-semibold text-gray-800 mt-1.5">
      {locked ? (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-gray-400 bg-gray-50 border border-gray-200 px-2 py-0.5 rounded shadow-sm">
          <Lock className="h-2.5 w-2.5 text-gray-400" />
          Confidentiel
        </span>
      ) : (
        value || <span className="text-gray-400 italic text-[11px]">Non renseigné</span>
      )}
    </div>
  </div>
);

// ── Composant pour une carte badge ───────────────────────────────────────────
const BadgeCard = ({ icon: Icon, label, badgeText, badgeClass, locked = false }) => (
  <div className="bg-slate-50 border border-slate-100 rounded-2xl p-4">
    <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider flex items-center gap-1.5">
      {Icon && <Icon className="h-3 w-3" />}
      {label}
    </p>
    <div className="mt-1.5">
      {locked ? (
        <span className="inline-flex items-center gap-1 text-[10px] font-bold text-gray-400 bg-gray-50 border border-gray-200 px-2 py-0.5 rounded shadow-sm">
          <Lock className="h-2.5 w-2.5 text-gray-400" />
          Confidentiel
        </span>
      ) : badgeText ? (
        <span className={`inline-flex items-center text-[10px] font-bold px-2.5 py-1 rounded-lg border ${badgeClass || 'bg-blue-50 text-blue-700 border-blue-100'}`}>
          {badgeText}
        </span>
      ) : (
        <span className="text-gray-400 italic text-xs">Non renseigné</span>
      )}
    </div>
  </div>
);

export default function EmployeeProfileDetail({ profileId, navigate, onShowToast }) {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [detailTab, setDetailTab] = useState('infos');
  const [status, setStatus] = useState(null);

  const { user } = useAuthStore();

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await apiClient.get(`/api/v1/hr/profiles/${profileId}`);
        setProfile(res.data);
      } catch (err) {
        console.error("Erreur lors de la récupération du profil:", err);
        setError("Profil non trouvé ou erreur de communication.");
        if (onShowToast) {
          onShowToast("Erreur lors du chargement du profil.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    const fetchStatus = async () => {
      try {
        const res = await apiClient.get(`/api/v1/employees/${profileId}/status`);
        setStatus(res.data.status);
      } catch (err) {
        console.error("Erreur lors de la récupération du statut:", err);
        setStatus("Absent");
      }
    };

    if (profileId) {
      fetchProfile();
      fetchStatus();
    }
  }, [profileId]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-gray-400">
        <Clock className="h-8 w-8 mx-auto mb-2 text-blue-500 animate-spin" />
        <p className="text-xs">Chargement des détails du profil...</p>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-12 text-center max-w-lg mx-auto mt-6">
        <AlertCircle className="h-12 w-12 text-rose-500 mx-auto mb-4" />
        <h2 className="text-lg font-bold text-gray-800">Erreur de chargement</h2>
        <p className="text-sm text-gray-500 mt-2">{error || "Profil introuvable."}</p>
        <button
          onClick={() => navigate('/tableau-de-bord')}
          className="mt-6 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg transition-colors"
        >
          Retourner au tableau de bord
        </button>
      </div>
    );
  }

  // ── RBAC : Détermination côté frontend (reflet de la logique backend) ────
  const isOwner = user && (
    (profile.userId && user.id === profile.userId) ||
    (profile.email && user.email?.toLowerCase() === profile.email?.toLowerCase())
  );
  const isHrOrDirection = user?.roles?.some(r => ['HR_ADMIN', 'DIRECTION'].includes(r));
  const isAuthorizedRole = user?.roles?.some(r => ['HR_ADMIN', 'DIRECTION', 'MANAGER'].includes(r));
  // La vraie autorisation pour les données [P] vient du backend (champs null si non autorisé)
  // Côté frontend, on vérifie juste si le champ est non-null pour afficher la section
  const hasPrivateData = profile.typeContrat != null || profile.seniorityLevel != null || profile.payFrequency != null || profile.employmentFraction != null;

  const initials = `${profile.firstName?.charAt(0) || ''}${profile.lastName?.charAt(0) || ''}`.toUpperCase();

  const workModelInfo = WORK_MODEL_LABELS[profile.workModel] || { label: profile.workModel, color: 'bg-gray-50 text-gray-600 border-gray-200' };
  const seniorityInfo = SENIORITY_LABELS[profile.seniorityLevel] || null;

  return (
    <div className="max-w-4xl mx-auto py-6">
      {/* Back button */}
      <button
        onClick={() => {
          if (isAuthorizedRole) {
            navigate('/profils');
          } else {
            navigate('/tableau-de-bord');
          }
        }}
        className="inline-flex items-center gap-2 text-xs font-bold text-gray-500 hover:text-blue-600 bg-white border border-gray-200 px-3.5 py-2 rounded-xl shadow-sm hover:shadow transition-all mb-6 active:scale-95"
      >
        <ArrowLeft className="h-3.5 w-3.5" />
        {isAuthorizedRole ? 'Retour à la liste des profils' : 'Retour au tableau de bord'}
      </button>

      {/* Main Container */}
      <div className="bg-white/95 backdrop-blur-md border border-gray-200 rounded-3xl shadow-xl overflow-hidden">

        {/* ── Banner ───────────────────────────────────────────────────────── */}
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6 md:p-8 text-white flex flex-col md:flex-row items-center gap-5">
          <div className="h-20 w-20 rounded-3xl bg-white/10 border-2 border-white/20 shadow flex items-center justify-center shrink-0">
            {profile.photoUrl ? (
              <img src={profile.photoUrl} alt="Photo" className="h-full w-full rounded-3xl object-cover" />
            ) : (
              <span className="text-2xl font-extrabold tracking-wider">{initials}</span>
            )}
          </div>
          <div className="text-center md:text-left flex-1 min-w-0">
            <h1 className="text-xl md:text-2xl font-black tracking-tight">{profile.firstName} {profile.lastName}</h1>
            <p className="text-sm font-semibold text-blue-100 mt-1">{profile.jobTitle}</p>
            <div className="flex flex-wrap items-center gap-2 mt-2 justify-center md:justify-start">
              <span className="text-xs text-blue-200/80 flex items-center gap-1">
                <Building2 className="h-3 w-3" />
                {profile.department}
              </span>
              {profile.workModel && (
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${workModelInfo.color}`}>
                  {workModelInfo.label}
                </span>
              )}
              {seniorityInfo && (
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border ${seniorityInfo.color}`}>
                  {seniorityInfo.label}
                </span>
              )}
              {status && (
                <span className={`text-[10px] font-extrabold px-2 py-0.5 rounded-full border ${
                  status.includes('bureau') || status.includes('télétravail')
                    ? 'bg-emerald-500/20 text-emerald-100 border-emerald-400/30'
                    : status.includes('Congé')
                      ? 'bg-amber-500/20 text-amber-100 border-amber-400/30'
                      : 'bg-rose-500/20 text-rose-100 border-rose-400/30'
                }`}>
                  ● {status}
                </span>
              )}
            </div>
          </div>
          <div className="flex flex-col items-center md:items-end gap-1.5 shrink-0 bg-white/10 border border-white/10 rounded-2xl p-4 text-xs">
            <div>
              <p className="text-blue-200 font-bold text-[9px] uppercase tracking-wider">Matricule</p>
              <p className="font-extrabold mt-0.5">#{profile.matricule}</p>
            </div>
            <div className="mt-2 text-right">
              <p className="text-blue-200 font-bold text-[9px] uppercase tracking-wider">Adresse E-mail</p>
              <p className="font-semibold mt-0.5 truncate max-w-[180px]">{profile.email}</p>
            </div>
            {profile.timeInJob && (
              <div className="mt-2 text-right">
                <p className="text-blue-200 font-bold text-[9px] uppercase tracking-wider">Ancienneté</p>
                <p className="font-semibold mt-0.5">{profile.timeInJob}</p>
              </div>
            )}
          </div>
        </div>

        {/* ── Tab Headers ─────────────────────────────────────────────────── */}
        <div className="flex border-b border-gray-100 px-6 md:px-8">
          {[
            ['infos', 'Informations'],
            ...(isOwner || isHrOrDirection ? [
              ['skills', `Compétences (${profile.skills?.length || 0})`],
              ['documents', `Documents RH (${profile.documents?.length || 0})`]
            ] : [])
          ].map(([id, label]) => (
            <button
              key={id}
              onClick={() => setDetailTab(id)}
              className={`py-4 mr-8 text-xs font-bold border-b-2 transition-colors focus:outline-none
                ${detailTab === id ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-400 hover:text-gray-600'}`}
            >
              {label}
            </button>
          ))}
        </div>

        {/* ── Tab Body ────────────────────────────────────────────────────── */}
        <div className="p-6 md:p-8">
          {detailTab === 'infos' ? (
            <div className="space-y-8">

              {/* ━━━ SECTION PUBLIQUE [E] ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */}
              <div>
                <div className="flex items-center gap-2 mb-4">
                  <Globe className="h-3.5 w-3.5 text-blue-500" />
                  <h3 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Informations Publiques</h3>
                  <span className="text-[9px] bg-blue-50 text-blue-600 border border-blue-100 px-1.5 py-0.5 rounded font-bold">Visible par tous</span>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

                  <InfoCard
                    icon={Briefcase}
                    label="Poste occupé"
                    value={profile.jobTitle}
                  />
                  <InfoCard
                    icon={Building2}
                    label="Département"
                    value={profile.department}
                  />
                  <InfoCard
                    icon={Calendar}
                    label="Date d'entrée"
                    value={profile.hireDate
                      ? new Date(profile.hireDate).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
                      : null}
                  />
                  <InfoCard
                    icon={Timer}
                    label="Ancienneté"
                    value={profile.timeInJob}
                  />
                  <BadgeCard
                    icon={Laptop}
                    label="Mode de travail"
                    badgeText={workModelInfo.label}
                    badgeClass={workModelInfo.color}
                  />
                  <InfoCard
                    icon={MapPin}
                    label="Localisation"
                    value={profile.location}
                  />
                  <InfoCard
                    icon={Globe}
                    label="Langues parlées"
                    value={profile.spokenLanguages}
                    fullWidth
                  />

                </div>
              </div>

              {/* ━━━ RATTACHEMENT HIÉRARCHIQUE [E] ━━━━━━━━━━━━━━━━━━━━━━━━━ */}
              <div>
                <div className="flex items-center gap-2 mb-4">
                  <Network className="h-3.5 w-3.5 text-indigo-500" />
                  <h3 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Rattachement Hiérarchique</h3>
                </div>
                <div className="bg-slate-50 border border-slate-100 rounded-2xl p-4">
                  {profile.managerName ? (
                    <div className="flex items-center gap-3">
                      <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-indigo-500 to-blue-500 flex items-center justify-center text-white font-bold text-xs shrink-0">
                        {profile.managerName.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <p className="text-xs font-bold text-gray-800">{profile.managerName}</p>
                        <p className="text-[10px] text-gray-500 mt-0.5">
                          {profile.managerMatricule && <span className="font-mono text-indigo-600 mr-1">#{profile.managerMatricule}</span>}
                          {profile.managerRole && <span className="bg-indigo-50 border border-indigo-100 text-indigo-700 px-1.5 py-0.5 rounded text-[9px] font-bold">{profile.managerRole}</span>}
                        </p>
                      </div>
                    </div>
                  ) : (
                    <p className="text-xs font-semibold text-gray-600 flex items-center gap-2">
                      <TrendingUp className="h-4 w-4 text-amber-500" />
                      Direction Générale — Aucun rattachement direct
                    </p>
                  )}
                </div>
              </div>

              {/* ━━━ SECTION PRIVÉE [P] ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */}
              {hasPrivateData && (
                <div>
                  <div className="flex items-center gap-2 mb-4">
                    <ShieldCheck className="h-3.5 w-3.5 text-amber-500" />
                    <h3 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">Informations Contractuelles</h3>
                    <span className="text-[9px] bg-amber-50 text-amber-700 border border-amber-100 px-1.5 py-0.5 rounded font-bold">Accès accordé</span>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <InfoCard
                      icon={CreditCard}
                      label="CIN / Identité"
                      value={profile.cin}
                    />
                    <InfoCard
                      icon={Phone}
                      label="Contact téléphone"
                      value={profile.contact}
                    />
                    <InfoCard
                      icon={MapPin}
                      label="Adresse personnelle"
                      value={profile.adresse}
                      fullWidth
                    />
                    <BadgeCard
                      icon={FileText}
                      label="Type de contrat"
                      badgeText={profile.typeContrat}
                      badgeClass="bg-blue-50 text-blue-700 border-blue-100"
                    />
                    <InfoCard
                      icon={Calendar}
                      label="Début de contrat"
                      value={profile.dateDebutContrat
                        ? new Date(profile.dateDebutContrat).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
                        : null}
                    />
                    {profile.dureeContrat && (
                      <InfoCard
                        icon={Timer}
                        label="Durée de contrat"
                        value={`${profile.dureeContrat} mois`}
                      />
                    )}
                    <BadgeCard
                      icon={TrendingUp}
                      label="Niveau d'expérience"
                      badgeText={seniorityInfo?.label || profile.seniorityLevel}
                      badgeClass={seniorityInfo?.color || 'bg-gray-50 text-gray-700 border-gray-200'}
                    />
                    <BadgeCard
                      icon={Briefcase}
                      label="Quotité de travail"
                      badgeText={FRACTION_LABELS[profile.employmentFraction] || profile.employmentFraction}
                      badgeClass="bg-emerald-50 text-emerald-700 border-emerald-100"
                    />
                    {profile.payFrequency && (
                      <InfoCard
                        icon={CreditCard}
                        label="Fréquence de paye"
                        value={profile.payFrequency === 'MONTHLY' ? 'Mensuelle' : profile.payFrequency}
                      />
                    )}
                    {profile.signatureNumerique && (
                      <div className="col-span-1 md:col-span-2 bg-slate-50 border border-slate-100 rounded-2xl p-4 flex items-center gap-3">
                        <Signature className="h-4 w-4 text-gray-400 shrink-0" />
                        <div>
                          <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Signature électronique</p>
                          <p className="text-[10px] font-mono text-gray-500 mt-0.5">{profile.signatureNumerique}</p>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}

            </div>
          ) : (detailTab === 'skills' && (isOwner || isHrOrDirection)) ? (
            (() => {
              const skillsToDisplay = profile.skills || [];
              if (skillsToDisplay.length === 0) {
                return (
                  <p className="text-xs text-gray-500 italic p-6 text-center bg-slate-50 rounded-2xl border border-slate-100">
                    Aucune compétence répertoriée.
                  </p>
                );
              }

              return (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {Array.from(skillsToDisplay).map((s, idx) => {
                    const percentage = (s.proficiencyLevel || 3) * 20;
                    const levelName = getSkillLevelName(s.proficiencyLevel);
                    const color = getSkillColor(s.category);
                    return (
                      <div key={idx} className="p-4 bg-slate-50 border border-slate-100 rounded-2xl shadow-sm">
                        <div className="flex items-center justify-between mb-2">
                          <p className="text-xs font-bold text-gray-800 truncate">{s.name}</p>
                          <div className="flex items-center gap-1.5 shrink-0">
                            <span className="text-[9px] font-bold text-blue-600 bg-blue-50 border border-blue-100 px-1.5 py-0.5 rounded">
                              {s.category}
                            </span>
                            <span className="text-[10px] font-semibold text-gray-500">{levelName} ({s.proficiencyLevel}/5)</span>
                          </div>
                        </div>
                        <div className="h-1.5 w-full bg-gray-200 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all duration-500 ${color}`}
                            style={{ width: `${percentage}%` }}
                          />
                        </div>
                      </div>
                    );
                  })}
                </div>
              );
            })()
          ) : (detailTab === 'documents' && (isOwner || isHrOrDirection)) ? (
            // Documents tab
            !hasPrivateData ? (
              <div className="text-center py-12 text-gray-400 bg-slate-50 rounded-2xl border border-slate-100">
                <Lock className="h-8 w-8 mx-auto mb-2 text-gray-300" />
                <p className="text-xs font-semibold text-gray-700">Accès restreint aux documents</p>
                <p className="text-[10px] text-gray-400 mt-1 max-w-sm mx-auto">
                  Par mesure de confidentialité, seuls les membres des ressources humaines et le propriétaire du profil peuvent consulter les documents contractuels.
                </p>
              </div>
            ) : !profile.documents?.length ? (
              <div className="text-center py-12 text-gray-400 bg-slate-50 rounded-2xl border border-slate-100">
                <FileText className="h-8 w-8 mx-auto mb-2 text-gray-300" />
                <p className="text-xs font-semibold">Aucun document dans le coffre-fort</p>
              </div>
            ) : (
              <div className="space-y-3">
                {Array.from(profile.documents).map((doc, idx) => (
                  <div key={idx} className="flex items-center justify-between p-4 bg-slate-50 hover:bg-slate-100/50 border border-slate-100 rounded-2xl transition-all shadow-sm">
                    <div className="flex items-center gap-3.5 min-w-0">
                      <div className="p-2.5 rounded-xl bg-white border border-slate-200 shrink-0 text-slate-500">
                        <FileText className="h-4.5 w-4.5" />
                      </div>
                      <div className="min-w-0">
                        <p className="text-xs font-bold text-gray-800 truncate">{doc.documentType}</p>
                        <p className="text-[10px] text-gray-400 font-mono truncate max-w-[250px]">{doc.filePath}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3 shrink-0">
                      {doc.isSigned ? (
                        <span className="inline-flex items-center gap-1 text-[9px] font-bold text-green-700 bg-green-50 border border-green-200 px-2 py-0.5 rounded-full">
                          <CheckCircle className="h-3 w-3" />
                          Signé
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[9px] font-bold text-amber-700 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-full">
                          <Clock className="h-3 w-3" />
                          En attente
                        </span>
                      )}
                      <span className="text-[10px] text-gray-400 font-medium">
                        {new Date(doc.uploadDate).toLocaleDateString('fr-FR')}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )
          ) : null}
        </div>

      </div>
    </div>
  );
}
