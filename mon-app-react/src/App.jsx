import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import useAuthStore from './store/useAuthStore';
import useAppStore from './store/useAppStore';
import apiClient from './api/apiClient';
import nexusLogo from './assets/NEXUS-LOGO.png';
import TimePunchCard from './components/TimePunchCard';
import LeaveRequestEmployeeView from './components/LeaveRequestEmployeeView';
import LeaveManagerDashboard from './components/LeaveManagerDashboard';
import AIAnalyticsDashboard from './components/AIAnalyticsDashboard';
import SmartAssistantWidget from './components/SmartAssistantWidget';
import DocumentsVaultView from './components/DocumentsVaultView';
import SkillsMatrixView from './components/SkillsMatrixView';
import EmployeeProfileDetail from './components/EmployeeProfileDetail';
import SettingsView from './components/SettingsView';
import NotificationDropdown from './components/NotificationDropdown';
import {
  Lock,
  Mail,
  Users,
  User,
  LogOut,
  Search,
  Plus,
  Trash2,
  Pencil,
  Eye,
  ChevronRight,
  CheckCircle,
  Clock,
  FileText,
  Award,
  Building2,
  AlertCircle,
  X,
  CreditCard,
  ShieldCheck,
  LayoutDashboard,
  Bell,
  ChevronDown,
  MoreHorizontal,
  ArrowUpRight,
  Filter,
  Calendar,
  Settings,
} from 'lucide-react';

// ─── Role Protected Route ──────────────────────────────────────────────────────
const RoleProtectedRoute = ({ allowedRoles, children, navigate }) => {
  const { user, isAuthenticated } = useAuthStore();
  
  if (!isAuthenticated) return null;
  
  const hasAllowedRole = user?.roles?.some(role => allowedRoles.includes(role));
  
  if (!hasAllowedRole) {
    return (
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-12 text-center max-w-lg mx-auto mt-12">
        <div className="h-12 w-12 text-red-500 mx-auto mb-4 bg-red-50 rounded-full flex items-center justify-center">
          <AlertCircle className="h-6 w-6" />
        </div>
        <h2 className="text-lg font-bold text-gray-800">403 Accès Refusé</h2>
        <p className="text-sm text-gray-500 mt-2">
          Vous n'avez pas les autorisations nécessaires pour accéder à cet espace.
        </p>
        <button
          onClick={() => navigate('/tableau-de-bord')}
          className="mt-6 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg transition-colors"
        >
          Retourner au tableau de bord
        </button>
      </div>
    );
  }
  
  return children;
};

// ─── Main App ──────────────────────────────────────────────────────────────────
function App() {
  const { t, i18n } = useTranslation();
  const { user, isAuthenticated, login, logout, _hasHydrated } = useAuthStore();
  const settings = useAppStore(state => state.settings);

  // Sync theme
  useEffect(() => {
    if (settings.theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [settings.theme]);

  // Sync language
  useEffect(() => {
    if (settings.language) {
      i18n.changeLanguage(settings.language);
    }
  }, [settings.language, i18n]);

  // Login form
  const [email, setEmail]             = useState('');
  const [password, setPassword]       = useState('');
  const [loginLoading, setLoginLoading] = useState(false);

  // Dashboard data
  const [profiles, setProfiles]       = useState([]);
  const [loading, setLoading]         = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDept, setSelectedDept] = useState('ALL');

  // Virtual Router State
  const [currentPath, setCurrentPath] = useState(window.location.pathname);
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const searchMenuRef = React.useRef(null);

  // Profile detail drawer
  const [viewingProfile, setViewingProfile] = useState(null);
  const [detailTab, setDetailTab]           = useState('skills');

  // Create / Edit modal
  const [isFormOpen, setIsFormOpen]       = useState(false);
  const [editingProfile, setEditingProfile] = useState(null);
  const [formUserId, setFormUserId]       = useState('');
  const [formMatricule, setFormMatricule] = useState('');
  const [formJobTitle, setFormJobTitle]   = useState('');
  const [formDept, setFormDept]           = useState('');
  const [formRib, setFormRib]             = useState('');
  const [formCin, setFormCin]             = useState('');
  const [formAdresse, setFormAdresse]     = useState('');
  const [formContact, setFormContact]     = useState('');
  const [formTypeContrat, setFormTypeContrat] = useState('CDI');
  const [formDateDebutContrat, setFormDateDebutContrat] = useState('');
  const [formDureeContrat, setFormDureeContrat] = useState('');
  const [formHierarchieId, setFormHierarchieId] = useState('');
  const [formPhotoUrl, setFormPhotoUrl]   = useState('');
  const [formSignatureNumerique, setFormSignatureNumerique] = useState('');
  const [formLoading, setFormLoading]     = useState(false);

  // Toast
  const currentUserProfile = profiles.find(p => p.email?.toLowerCase() === user?.email?.toLowerCase());
  const [toast, setToast] = useState(null);
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const profileMenuRef = React.useRef(null);
  
  const [notifications, setNotifications] = useState([]);
  const [count, setCount] = useState(0);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const notificationsRef = React.useRef(null);

  useEffect(() => {
    setCount(notifications.length);
  }, [notifications]);

  const fetchNotifications = async () => {
    try {
      const res = await apiClient.get('/api/v1/notifications/unread');
      setNotifications(res.data);
    } catch (err) {
      console.error("Erreur lors de la récupération des notifications:", err);
    }
  };

  const markAsRead = async (id) => {
    try {
      await apiClient.patch(`/api/v1/notifications/${id}/read`);
      setNotifications(prev => prev.filter(n => n.id !== id));
      showToast("Notification marquée comme lue", "success");
    } catch (err) {
      console.error("Erreur lors du marquage comme lu:", err);
      showToast("Erreur lors du marquage de la notification", "error");
    }
  };

  useEffect(() => {
    function handleClickOutside(event) {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target)) {
        setIsProfileMenuOpen(false);
      }
      if (notificationsRef.current && !notificationsRef.current.contains(event.target)) {
        setIsNotificationsOpen(false);
      }
      if (searchMenuRef.current && !searchMenuRef.current.contains(event.target)) {
        setIsSearchFocused(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [profileMenuRef, notificationsRef, searchMenuRef]);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 4000);
  };

  const navigate = (path) => {
    window.history.pushState(null, '', path);
    setCurrentPath(path);
  };

  useEffect(() => {
    const handlePopState = () => {
      setCurrentPath(window.location.pathname);
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  useEffect(() => {
    if (currentPath === '/' || currentPath === '/index.html') {
      window.history.replaceState(null, '', '/tableau-de-bord');
      setCurrentPath('/tableau-de-bord');
    }
  }, [currentPath]);

  const fetchProfiles = async () => {
    setLoading(true);
    try {
      const res = await apiClient.get('/api/v1/hr/profiles');
      const data = res.data.map(p => {
        const job = (p.jobTitle || '').toLowerCase();
        const dept = (p.department || '').toLowerCase();
        
        let defaultSkills = [
          { name: 'Gestion de projet', category: 'Management', proficiencyLevel: 4 },
          { name: 'Négociation commerciale', category: 'Management', proficiencyLevel: 3 },
          { name: 'Leadership & Stratégie', category: 'Management', proficiencyLevel: 3 }
        ];
        
        if (job.includes('dev') || job.includes('tech') || job.includes('backend') || job.includes('frontend') || job.includes('ingénieur') || dept.includes('eng') || dept.includes('r&d')) {
          defaultSkills = [
            { name: 'Java / Spring Boot', category: 'Technique', proficiencyLevel: 4 },
            { name: 'React / Tailwind CSS', category: 'Technique', proficiencyLevel: 3 },
            { name: 'Docker / CI-CD', category: 'Technique', proficiencyLevel: 3 }
          ];
        } else if (job.includes('rh') || job.includes('hr') || job.includes('recrut') || dept.includes('rh') || dept.includes('hr') || job.includes('admin')) {
          defaultSkills = [
            { name: 'Recrutement & Sourcing', category: 'RH', proficiencyLevel: 5 },
            { name: 'Droit du Travail', category: 'RH', proficiencyLevel: 4 },
            { name: 'GPEC', category: 'RH', proficiencyLevel: 4 }
          ];
        } else if (job.includes('market') || job.includes('comm') || dept.includes('market')) {
          defaultSkills = [
            { name: 'Stratégie Marketing Digital', category: 'Marketing', proficiencyLevel: 4 },
            { name: 'SEO & SEA Google Ads', category: 'Marketing', proficiencyLevel: 3 },
            { name: 'Analyse d\'audience web', category: 'Technique', proficiencyLevel: 3 }
          ];
        }

        const skills = p.skills && p.skills.length > 0 ? p.skills : defaultSkills;

        // Inject mock documents if none exist
        const documents = p.documents && p.documents.length > 0 ? p.documents : [
          { documentType: 'Contrat de travail (CDI)', filePath: 'contrat_cdi.pdf', isSigned: true, uploadDate: '2026-05-15' },
          { documentType: 'Fiche de paie - Mai 2026', filePath: 'fiche_paie_mai.pdf', isSigned: true, uploadDate: '2026-06-01' }
        ];

        return { ...p, skills, documents };
      });
      setProfiles(data);

      // Enrich log-in user with profileId if not already present
      if (user && !user.profileId) {
        const myProfile = data.find(
          (p) => p.email && p.email.toLowerCase() === user.email.toLowerCase()
        );
        if (myProfile) {
          login(useAuthStore.getState().token, {
            ...user,
            profileId: myProfile.id,
            firstName: myProfile.firstName,
            lastName: myProfile.lastName
          });
        }
      }
    } catch {
      showToast('Erreur lors de la récupération des profils.', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchProfiles();
      fetchNotifications();
    }
  }, [isAuthenticated]);

  // ── Auth ──────────────────────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault();
    if (!email || !password) { showToast('Veuillez remplir tous les champs.', 'error'); return; }
    setLoginLoading(true);
    try {
      const res = await apiClient.post('/api/auth/login', { email, password });
      const { token: jwtToken, id, email: resEmail, firstName, lastName, roles } = res.data;
      const name = `${firstName} ${lastName}`;
      login(jwtToken, { id, email: resEmail, name, firstName, lastName, roles });
      showToast(`Bienvenue, ${name} !`);
    } catch (err) {
      showToast(err.response?.data?.message || 'Identifiants invalides.', 'error');
    } finally {
      setLoginLoading(false);
    }
  };

  const hasAdminRole = () => user?.roles?.some(r => ['HR_ADMIN', 'DIRECTION'].includes(r));
  const isManagerOrAdmin = () => user?.roles?.some(r => ['HR_ADMIN', 'MANAGER', 'DIRECTION'].includes(r));

  const navItems = [
    { id: 'dashboard', label: t('menu.dashboard', 'Tableau de bord'), icon: LayoutDashboard, path: '/tableau-de-bord' },
    ...(isManagerOrAdmin() ? [{ id: 'profiles',  label: t('menu.profiles', 'Profils employés'), icon: Users, path: '/profils' }] : []),
    { id: 'attendance', label: t('menu.attendance', 'Temps & Présences'), icon: Clock, path: '/temps' },
    ...(isManagerOrAdmin() 
      ? [{ id: 'leave-validation', label: t('menu.leave_validation', 'Validation RH'), icon: ShieldCheck, path: '/validation-conges' }] 
      : [{ id: 'leave-requests', label: t('menu.leave_requests', 'Mes Congés'), icon: Calendar, path: '/conges' }]),
    { id: 'documents', label: t('menu.documents', 'Documents RH'),     icon: FileText, path: '/documents' },
    { id: 'skills',    label: t('menu.skills', 'Compétences'),       icon: Award, path: '/competences' },
    { id: 'settings',  label: t('menu.settings', 'Paramètres'),       icon: Settings, path: '/parametres' },
  ];

  const activeNavItem = navItems.find(item => {
    if (item.path === '/profils' && currentPath.startsWith('/profil')) {
      return true;
    }
    return item.path === currentPath;
  });
  const activeNav = activeNavItem ? activeNavItem.id : 'profiles';

  // ── CRUD ──────────────────────────────────────────────────────────────────
  const handleSaveProfile = async (e) => {
    e.preventDefault();
    if (!formUserId || !formJobTitle || !formDept) { showToast('Champs obligatoires manquants.', 'error'); return; }
    setFormLoading(true);
    try {
      const payload = {
        userId: parseInt(formUserId),
        matricule: formMatricule || null,
        jobTitle: formJobTitle,
        department: formDept,
        rib: formRib || null,
        cin: formCin || null,
        adresse: formAdresse || null,
        contact: formContact || null,
        typeContrat: formTypeContrat || null,
        dateDebutContrat: formDateDebutContrat || null,
        dureeContrat: formDureeContrat !== '' ? parseInt(formDureeContrat) : null,
        hierarchieId: formHierarchieId !== '' ? parseInt(formHierarchieId) : null,
        photoUrl: formPhotoUrl || null,
        signatureNumerique: formSignatureNumerique || null
      };
      if (editingProfile) {
        const res = await apiClient.put(`/api/v1/hr/profiles/${editingProfile.id}`, payload);
        setProfiles(profiles.map(p => p.id === editingProfile.id ? res.data : p));
        showToast('Profil mis à jour avec succès.');
      } else {
        const res = await apiClient.post('/api/v1/hr/profiles', payload);
        setProfiles([...profiles, res.data]);
        showToast('Profil créé avec succès.');
      }
      handleCloseForm();
    } catch (err) {
      showToast(err.response?.data?.message || "Erreur lors de l'enregistrement.", 'error');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteProfile = async (id, e) => {
    e.stopPropagation();
    if (!hasAdminRole()) { showToast('Permissions insuffisantes.', 'error'); return; }
    if (!window.confirm('Supprimer définitivement ce profil ?')) return;
    try {
      await apiClient.delete(`/api/v1/hr/profiles/${id}`);
      setProfiles(profiles.filter(p => p.id !== id));
      if (viewingProfile?.id === id) setViewingProfile(null);
      showToast('Profil supprimé.');
    } catch (err) {
      showToast(err.response?.data?.message || 'Erreur lors de la suppression.', 'error');
    }
  };

  const handleOpenEdit = (profile, e) => {
    e.stopPropagation();
    setEditingProfile(profile);
    setFormUserId(profile.userId);
    setFormMatricule(profile.matricule || '');
    setFormJobTitle(profile.jobTitle);
    setFormDept(profile.department);
    setFormRib(profile.rib || '');
    setFormCin(profile.cin || '');
    setFormAdresse(profile.adresse || '');
    setFormContact(profile.contact || '');
    setFormTypeContrat(profile.typeContrat || 'CDI');
    setFormDateDebutContrat(profile.dateDebutContrat || '');
    setFormDureeContrat(profile.dureeContrat !== null && profile.dureeContrat !== undefined ? profile.dureeContrat : '');
    setFormHierarchieId(profile.hierarchieId || '');
    setFormPhotoUrl(profile.photoUrl || '');
    setFormSignatureNumerique(profile.signatureNumerique || '');
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false); setEditingProfile(null);
    setFormUserId(''); setFormJobTitle(''); setFormDept(''); setFormRib('');
    setFormCin(''); setFormAdresse(''); setFormContact(''); setFormTypeContrat('CDI');
    setFormDateDebutContrat(''); setFormDureeContrat(''); setFormHierarchieId('');
    setFormPhotoUrl(''); setFormSignatureNumerique('');
    setFormMatricule('');
  };

  const filteredProfiles = profiles.filter(p => {
    const q = searchQuery.toLowerCase();
    const matchSearch = [p.firstName, p.lastName, p.email, p.jobTitle, p.department]
      .some(v => (v || '').toLowerCase().includes(q));
    const matchDept = selectedDept === 'ALL' || p.department === selectedDept;
    return matchSearch && matchDept;
  });

  const departments = ['ALL', ...new Set(profiles.map(p => p.department).filter(Boolean))];

  // ── Derived stats ──────────────────────────────────────────────────────────
  const totalSkills = profiles.reduce((a, p) => a + (p.skills?.length || 0), 0);
  const totalDocs   = profiles.reduce((a, p) => a + (p.documents?.length || 0), 0);
  const activeDepts = new Set(profiles.map(p => p.department).filter(Boolean)).size;

  // ═══════════════════════════════════════════════════════════════════════════
  // HYDRATION GATE — prevent flickering while Zustand restores from localStorage
  // ═══════════════════════════════════════════════════════════════════════════
  if (!_hasHydrated) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
        <img src={nexusLogo} alt="NEXUS ERP" className="h-12 w-auto object-contain mb-6 animate-pulse" />
        <div className="h-5 w-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // LOGIN PAGE — shown only when user is NOT authenticated
  // ═══════════════════════════════════════════════════════════════════════════
  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">

        {/* Toast */}
        {toast && (
          <div className={`fixed top-5 right-5 z-50 flex items-center gap-3 px-4 py-3 rounded-lg border shadow-md text-sm font-medium
            ${toast.type === 'success' ? 'bg-white border-green-200 text-green-800' : 'bg-white border-red-200 text-red-700'}`}>
            {toast.type === 'success'
              ? <CheckCircle className="h-4 w-4 text-green-500 shrink-0" />
              : <AlertCircle className="h-4 w-4 text-red-500 shrink-0" />}
            {toast.message}
          </div>
        )}

        <div className="w-full max-w-sm">
          {/* Logo */}
          <div className="flex justify-center mb-8">
            <img src={nexusLogo} alt="NEXUS ERP" className="h-14 w-auto object-contain" />
          </div>

          {/* Card */}
          <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-8">
            <h1 className="text-lg font-semibold text-gray-900 mb-1">Connexion</h1>
            <p className="text-sm text-gray-500 mb-6">Accédez à votre espace NEXUS ERP.</p>

            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1.5">Adresse e-mail</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  <input
                    type="email" value={email} onChange={e => setEmail(e.target.value)}
                    placeholder="prenom.nom@entreprise.com"
                    className="w-full pl-9 pr-3 py-2.5 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-400"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1.5">Mot de passe</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
                  <input
                    type="password" value={password} onChange={e => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-9 pr-3 py-2.5 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-400"
                  />
                </div>
              </div>

              <button
                type="submit" disabled={loginLoading}
                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white text-sm font-medium py-2.5 rounded-lg transition-colors flex items-center justify-center gap-2"
              >
                {loginLoading
                  ? <span className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  : 'Se connecter'}
              </button>
            </form>

            {/* Quick fill */}
            <div className="mt-6 pt-5 border-t border-gray-100">
              <p className="text-xs text-gray-400 mb-2.5 font-medium">Comptes de démonstration</p>
              <div className="grid grid-cols-2 gap-2">
                <button
                  onClick={() => { setEmail('sophie.laurent@dyxia.fr'); setPassword('admin123'); }}
                  className="text-left px-3 py-2 rounded-lg border border-gray-200 hover:bg-gray-50 hover:border-blue-300 transition-colors"
                >
                  <p className="text-xs font-semibold text-gray-800">Directrice RH</p>
                  <p className="text-[11px] text-gray-400">Accès complet</p>
                </button>
                <button
                  onClick={() => { setEmail('julien.morel@dyxia.fr'); setPassword('employee123'); }}
                  className="text-left px-3 py-2 rounded-lg border border-gray-200 hover:bg-gray-50 hover:border-gray-300 transition-colors"
                >
                  <p className="text-xs font-semibold text-gray-800">Julien (Employé)</p>
                  <p className="text-[11px] text-gray-400">Accès standard</p>
                </button>
              </div>
            </div>
          </div>

          <p className="text-center text-xs text-gray-400 mt-6">© 2026 DyxIA · NEXUS ERP · Données chiffrées AES-256</p>
        </div>
      </div>
    );
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // MAIN LAYOUT (Authenticated)
  // ═══════════════════════════════════════════════════════════════════════════
  return (
    <div className="min-h-screen bg-gray-50 flex font-sans text-sm">

      {/* ─── Toast ───────────────────────────────────────────────────────── */}
      {toast && (
        <div className={`fixed top-4 right-4 z-50 flex items-center gap-3 px-4 py-3 rounded-lg border shadow-md text-sm font-medium
          ${toast.type === 'success' ? 'bg-white border-green-200 text-green-800' : 'bg-white border-red-200 text-red-700'}`}>
          {toast.type === 'success'
            ? <CheckCircle className="h-4 w-4 text-green-500 shrink-0" />
            : <AlertCircle className="h-4 w-4 text-red-500 shrink-0" />}
          {toast.message}
        </div>
      )}

      {/* ─── SIDEBAR ─────────────────────────────────────────────────────── */}
      <aside className="w-56 shrink-0 fixed inset-y-0 left-0 bg-white border-r border-gray-200 flex flex-col z-20">
        {/* Logo */}
        <div className="h-14 flex items-center px-4 border-b border-gray-100">
          <img src={nexusLogo} alt="NEXUS ERP" className="h-8 w-auto object-contain" />
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto py-3 px-2">
          <p className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider px-2 mb-2">{t('menu.title', 'Menu principal')}</p>
          {navItems.map(({ id, label, icon: Icon, path }) => (
            <button
              key={id}
              onClick={() => navigate(path)}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors mb-0.5
                ${activeNav === id
                  ? 'bg-blue-50 text-blue-700 font-semibold'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'}`}
            >
              <Icon className={`h-4 w-4 shrink-0 ${activeNav === id ? 'text-blue-600' : 'text-gray-400'}`} />
              {label}
            </button>
          ))}
        </nav>

        {/* User profile footer */}
        <div className="p-3 border-t border-gray-100">
          <div 
            onClick={() => {
              if (user?.profileId) {
                navigate(`/profil/${user.profileId}`);
              } else {
                const myProfile = profiles.find(p => p.email?.toLowerCase() === user?.email?.toLowerCase());
                if (myProfile) navigate(`/profil/${myProfile.id}`);
                else showToast("Profil introuvable", "error");
              }
            }}
            className="flex items-center gap-2.5 px-2 py-2 rounded-lg hover:bg-gray-50 cursor-pointer group"
          >
            <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
              <span className="text-xs font-bold text-blue-700">
                {user?.name?.charAt(0) || 'U'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-gray-800 truncate">{user?.name}</p>
              <p className="text-[10px] text-gray-400 truncate">{user?.email}</p>
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation();
                logout();
              }}
              title={t('header.logout', 'Se déconnecter')}
              className="p-1 rounded text-gray-300 hover:text-red-500 transition-colors"
            >
              <LogOut className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      </aside>

      {/* ─── MAIN AREA (Topbar + Content) ────────────────────────────────── */}
      <div className="flex-1 ml-56 flex flex-col min-h-screen">

        {/* ─── TOPBAR ──────────────────────────────────────────────────── */}
        <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-6 fixed top-0 right-0 left-56 z-10">
          {/* Global search */}
          <div className="relative w-80" ref={searchMenuRef}>
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onFocus={() => setIsSearchFocused(true)}
              placeholder={t('header.search_placeholder', 'Rechercher un profil, département…')}
              className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent placeholder-gray-400 bg-gray-50 transition-all focus:bg-white focus:border-blue-500"
            />
            {/* Suggestions Dropdown */}
            {isSearchFocused && searchQuery.trim() !== '' && (
              <div className="absolute left-0 right-0 mt-2 bg-white border border-gray-200 rounded-xl shadow-lg max-h-60 overflow-y-auto z-40 py-1.5 animate-in fade-in duration-100">
                {profiles.filter(p => {
                  const q = searchQuery.toLowerCase();
                  return (
                    (p.firstName || '').toLowerCase().includes(q) ||
                    (p.lastName || '').toLowerCase().includes(q) ||
                    String(p.id).includes(q) ||
                    String(p.userId || '').includes(q) ||
                    (p.jobTitle || '').toLowerCase().includes(q) ||
                    (p.department || '').toLowerCase().includes(q)
                  );
                }).length === 0 ? (
                  <div className="px-4 py-2.5 text-xs text-gray-400 italic">
                    {t('header.no_results', 'Aucun résultat trouvé')}
                  </div>
                ) : (
                  profiles.filter(p => {
                    const q = searchQuery.toLowerCase();
                    return (
                      (p.firstName || '').toLowerCase().includes(q) ||
                      (p.lastName || '').toLowerCase().includes(q) ||
                      String(p.id).includes(q) ||
                      String(p.userId || '').includes(q) ||
                      (p.jobTitle || '').toLowerCase().includes(q) ||
                      (p.department || '').toLowerCase().includes(q)
                    );
                  }).map(p => (
                    <button
                      key={p.id}
                      onClick={() => {
                        navigate(`/profil/${p.id}`);
                        setSearchQuery('');
                        setIsSearchFocused(false);
                      }}
                      className="w-full flex items-center gap-3 px-3 py-2 hover:bg-gray-50 transition-colors text-left"
                    >
                      <div className="h-7 w-7 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                        <span className="text-[10px] font-bold text-blue-700">
                          {`${p.firstName?.charAt(0) || ''}${p.lastName?.charAt(0) || ''}`.toUpperCase()}
                        </span>
                      </div>
                      <div className="min-w-0">
                        <p className="text-xs font-semibold text-gray-800 truncate">
                          {p.firstName} {p.lastName}
                        </p>
                        <p className="text-[10px] text-gray-400 truncate">
                          {p.jobTitle} · {p.department}
                        </p>
                      </div>
                    </button>
                  ))
                )}
              </div>
            )}
          </div>

          {/* Right utilities */}
          <div className="flex items-center gap-3">
            {/* Settings Gear Icon */}
            <button
              onClick={() => navigate('/parametres')}
              className="p-2 rounded-lg hover:bg-gray-100 transition-colors text-gray-500"
              title={t('menu.settings', 'Paramètres')}
            >
              <Settings className="h-4 w-4" />
            </button>

            <div className="relative" ref={notificationsRef}>
              <button
                onClick={() => {
                  setIsNotificationsOpen(!isNotificationsOpen);
                  if (!isNotificationsOpen) {
                    fetchNotifications();
                  }
                }}
                className="relative p-2 rounded-lg hover:bg-gray-100 transition-colors text-gray-500"
              >
                <Bell className="h-4 w-4" />
                {notifications.length > 0 && (
                  <span className="absolute top-1.5 right-1.5 h-1.5 w-1.5 rounded-full bg-red-500 animate-pulse" />
                )}
              </button>

              {isNotificationsOpen && (
                <NotificationDropdown
                  notifications={notifications}
                  count={count}
                  onMarkAsRead={markAsRead}
                />
              )}
            </div>
            <div className="relative" ref={profileMenuRef}>
              <button
                onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
                className="flex items-center gap-2 pl-3 border-l border-gray-200 hover:bg-gray-50 cursor-pointer p-1.5 rounded-lg transition-colors text-left focus:outline-none"
              >
                <div className="h-7 w-7 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                  <span className="text-xs font-bold text-blue-700">{user?.name?.charAt(0) || 'U'}</span>
                </div>
                <div className="hidden md:block">
                  <p className="text-xs font-semibold text-gray-800 leading-none">{user?.name}</p>
                  <p className="text-[10px] text-gray-400 mt-0.5">
                    {hasAdminRole() ? t('header.admin_role', 'Administrateur RH') : t('header.employee_role', 'Employé')}
                  </p>
                </div>
                <ChevronDown className={`h-3.5 w-3.5 text-gray-400 transition-transform duration-200 ${isProfileMenuOpen ? 'rotate-180' : ''}`} />
              </button>

              {isProfileMenuOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-xl shadow-lg py-1.5 z-30 animate-in fade-in slide-in-from-top-2 duration-150">
                  <div className="px-4 py-2 border-b border-gray-100">
                    <p className="text-xs font-semibold text-gray-800 leading-none">{user?.name}</p>
                    <p className="text-[9px] text-gray-400 mt-1 truncate">{user?.email}</p>
                  </div>
                  
                  <button
                    onClick={() => {
                      setIsProfileMenuOpen(false);
                      if (user?.profileId) {
                        navigate(`/profil/${user.profileId}`);
                      } else {
                        const myProfile = profiles.find(p => p.email?.toLowerCase() === user?.email?.toLowerCase());
                        if (myProfile) navigate(`/profil/${myProfile.id}`);
                        else showToast("Profil introuvable", "error");
                      }
                    }}
                    className="w-full flex items-center gap-2 px-4 py-2 text-xs text-gray-700 hover:bg-gray-50 transition-colors text-left"
                  >
                    <User className="h-3.5 w-3.5 text-gray-400" />
                    {t('header.my_profile', 'Mon Profil')}
                  </button>

                  {isManagerOrAdmin() && (
                    <button
                      onClick={() => {
                        setIsProfileMenuOpen(false);
                        navigate('/profils');
                      }}
                      className="w-full flex items-center gap-2 px-4 py-2 text-xs text-gray-700 hover:bg-gray-50 transition-colors text-left"
                    >
                      <Users className="h-3.5 w-3.5 text-gray-400" />
                      {t('menu.profiles', 'Profils employés')}
                    </button>
                  )}

                  <button
                    onClick={() => {
                      setIsProfileMenuOpen(false);
                      navigate('/temps');
                    }}
                    className="w-full flex items-center gap-2 px-4 py-2 text-xs text-gray-700 hover:bg-gray-50 transition-colors text-left"
                  >
                    <Clock className="h-3.5 w-3.5 text-gray-400" />
                    {t('header.my_clock', 'Ma Pointeuse')}
                  </button>

                  <div className="border-t border-gray-100 my-1" />

                  <button
                    onClick={() => {
                      setIsProfileMenuOpen(false);
                      logout();
                    }}
                    className="w-full flex items-center gap-2 px-4 py-2 text-xs text-red-600 hover:bg-red-50 transition-colors text-left"
                  >
                    <LogOut className="h-3.5 w-3.5 text-red-500" />
                    {t('header.logout', 'Se déconnecter')}
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>

        {/* ─── CONTENT ─────────────────────────────────────────────────── */}
        <main className="flex-1 pt-14 p-6 overflow-y-auto">
          {(() => {
            const matchProfil = currentPath.match(/^\/profil\/(\d+)/);
            const matchedProfilId = matchProfil ? parseInt(matchProfil[1]) : null;

            if (currentPath === '/tableau-de-bord') {
              return <AIAnalyticsDashboard />;
            } else if (currentPath === '/parametres') {
              return <SettingsView onShowToast={showToast} />;
            } else if (currentPath === '/temps') {
              return (
                <div className="py-6 flex items-center justify-center">
                  <TimePunchCard onShowToast={showToast} />
                </div>
              );
            } else if (currentPath === '/profils') {
              return (
                <RoleProtectedRoute allowedRoles={['HR_ADMIN', 'MANAGER', 'DIRECTION']} navigate={navigate}>
                  {/* Page header */}
                  <div className="flex items-center justify-between mb-6">
                    <div>
                      <h1 className="text-base font-semibold text-gray-900">Profils employés</h1>
                      <p className="text-xs text-gray-500 mt-0.5">{profiles.length} collaborateur{profiles.length !== 1 ? 's' : ''} enregistré{profiles.length !== 1 ? 's' : ''}</p>
                    </div>
                    {hasAdminRole() && (
                      <button
                        onClick={() => setIsFormOpen(true)}
                        className="flex items-center gap-1.5 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold px-4 py-2 rounded-lg transition-colors"
                      >
                        <Plus className="h-3.5 w-3.5" />
                        Nouveau profil
                      </button>
                    )}
                  </div>

                  {/* ── Stats row ──────────────────────────────────────────────── */}
                  <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                    {[
                      { label: 'Collaborateurs', value: profiles.length, sub: 'Total dans le SIRH', icon: Users, color: 'text-blue-600', bg: 'bg-blue-50' },
                      { label: 'Départements', value: activeDepts, sub: 'Unités organisationnelles', icon: Building2, color: 'text-violet-600', bg: 'bg-violet-50' },
                      { label: 'Compétences', value: totalSkills, sub: 'Répertoriées', icon: Award, color: 'text-amber-600', bg: 'bg-amber-50' },
                      { label: 'Documents RH', value: totalDocs, sub: 'Dans le coffre-fort', icon: FileText, color: 'text-emerald-600', bg: 'bg-emerald-50' },
                    ].map(({ label, value, sub, icon: Icon, color, bg }) => (
                      <div key={label} className="bg-white border border-gray-200 rounded-xl p-4 flex items-start gap-3 shadow-sm">
                        <div className={`${bg} ${color} p-2 rounded-lg shrink-0`}>
                          <Icon className="h-4 w-4" />
                        </div>
                        <div>
                          <p className="text-xl font-bold text-gray-900 leading-none">{value}</p>
                          <p className="text-xs font-medium text-gray-700 mt-1">{label}</p>
                          <p className="text-[11px] text-gray-400">{sub}</p>
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* ── Data Table ─────────────────────────────────────────────── */}
                  <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
                    {/* Table toolbar */}
                    <div className="flex items-center justify-between px-4 py-3 border-b border-gray-100">
                      <h2 className="text-sm font-semibold text-gray-800">Liste des profils</h2>
                      <div className="flex items-center gap-2">
                        <div className="flex items-center gap-1.5 text-xs text-gray-500 border border-gray-200 rounded-lg px-3 py-1.5">
                          <Filter className="h-3.5 w-3.5 text-gray-400" />
                          <select
                            value={selectedDept}
                            onChange={e => setSelectedDept(e.target.value)}
                            className="bg-transparent outline-none cursor-pointer text-gray-600 text-xs"
                          >
                            <option value="ALL">Tous les départements</option>
                            {departments.filter(d => d !== 'ALL').map(d => (
                              <option key={d} value={d}>{d}</option>
                            ))}
                          </select>
                        </div>
                      </div>
                    </div>

                    {/* Table */}
                    {loading ? (
                      <div className="flex items-center justify-center py-16 text-gray-400">
                        <span className="h-6 w-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin mr-3" />
                        Chargement des données…
                      </div>
                    ) : filteredProfiles.length === 0 ? (
                      <div className="py-16 text-center">
                        <AlertCircle className="h-8 w-8 text-gray-300 mx-auto mb-2" />
                        <p className="text-sm font-medium text-gray-500">Aucun profil trouvé</p>
                        <p className="text-xs text-gray-400 mt-1">Ajustez vos filtres ou créez un nouveau profil.</p>
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                          <thead>
                            <tr className="bg-gray-50 border-b border-gray-100 text-left">
                              <th className="py-2.5 px-4 text-xs font-semibold text-gray-500">Collaborateur</th>
                              <th className="py-2.5 px-4 text-xs font-semibold text-gray-500">Département</th>
                              <th className="py-2.5 px-4 text-xs font-semibold text-gray-500">Poste</th>
                              <th className="py-2.5 px-4 text-xs font-semibold text-gray-500 text-center">Compétences</th>
                              <th className="py-2.5 px-4 text-xs font-semibold text-gray-500 text-center">Documents</th>
                              <th className="py-2.5 px-4 text-xs font-semibold text-gray-500 text-right">Actions</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-gray-50">
                            {filteredProfiles.map(p => {
                              const initials = `${p.firstName?.charAt(0) || ''}${p.lastName?.charAt(0) || ''}`.toUpperCase() || 'EP';
                              return (
                                <tr
                                  key={p.id}
                                  onClick={() => navigate(`/profil/${p.id}`)}
                                  className="hover:bg-gray-50 cursor-pointer transition-colors group"
                                >
                                  {/* Collaborateur */}
                                  <td className="py-3 px-4">
                                    <div className="flex items-center gap-3">
                                      <div className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center shrink-0">
                                        <span className="text-xs font-bold text-blue-700">{initials}</span>
                                      </div>
                                      <div>
                                        <p className="font-medium text-gray-900 text-xs">{p.firstName} {p.lastName}</p>
                                        <p className="text-[11px] text-gray-400">{p.email}</p>
                                      </div>
                                    </div>
                                  </td>
                                  {/* Département */}
                                  <td className="py-3 px-4">
                                    <span className="inline-flex items-center text-xs font-medium text-gray-600 bg-gray-100 px-2 py-0.5 rounded">
                                      {p.department || '—'}
                                    </span>
                                  </td>
                                  {/* Poste */}
                                  <td className="py-3 px-4 text-xs text-gray-700">{p.jobTitle || <span className="text-gray-400">Non renseigné</span>}</td>
                                  {/* Compétences */}
                                  <td className="py-3 px-4 text-center">
                                    <span className="text-xs font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded">
                                      {p.skills?.length || 0}
                                    </span>
                                  </td>
                                  {/* Documents */}
                                  <td className="py-3 px-4 text-center">
                                    <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded">
                                      {p.documents?.length || 0}
                                    </span>
                                  </td>
                                  {/* Actions */}
                                  <td className="py-3 px-4">
                                    <div className="flex items-center justify-end gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                      <button
                                        onClick={e => { e.stopPropagation(); navigate(`/profil/${p.id}`); }}
                                        className="p-1.5 rounded hover:bg-blue-50 text-gray-400 hover:text-blue-600 transition-colors"
                                        title="Voir le profil 360°"
                                      >
                                        <Eye className="h-3.5 w-3.5" />
                                      </button>
                                      {hasAdminRole() && (
                                        <>
                                          <button
                                            onClick={e => handleOpenEdit(p, e)}
                                            className="p-1.5 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-700 transition-colors"
                                            title="Modifier"
                                          >
                                            <Pencil className="h-3.5 w-3.5" />
                                          </button>
                                          <button
                                            onClick={e => handleDeleteProfile(p.id, e)}
                                            className="p-1.5 rounded hover:bg-red-50 text-gray-400 hover:text-red-600 transition-colors"
                                            title="Supprimer"
                                          >
                                            <Trash2 className="h-3.5 w-3.5" />
                                          </button>
                                        </>
                                      )}
                                    </div>
                                  </td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>

                        {/* Table footer */}
                        <div className="px-4 py-2.5 border-t border-gray-100 flex items-center justify-between">
                          <p className="text-xs text-gray-400">
                            Affichage de <span className="font-medium text-gray-700">{filteredProfiles.length}</span> sur <span className="font-medium text-gray-700">{profiles.length}</span> profils
                          </p>
                          <span className="text-[10px] text-gray-400 font-medium">NEXUS SIRH v2.0</span>
                        </div>
                      </div>
                    )}
                  </div>
                </RoleProtectedRoute>
              );
            } else if (currentPath === '/validation-conges') {
              return (
                <RoleProtectedRoute allowedRoles={['HR_ADMIN', 'MANAGER', 'DIRECTION']} navigate={navigate}>
                  <LeaveManagerDashboard onShowToast={showToast} />
                </RoleProtectedRoute>
              );
            } else if (currentPath === '/conges') {
              return <LeaveRequestEmployeeView onShowToast={showToast} />;
            } else if (currentPath === '/documents') {
              return <DocumentsVaultView onShowToast={showToast} />;
            } else if (currentPath === '/competences') {
              return <SkillsMatrixView userProfile={currentUserProfile} onShowToast={showToast} />;
            } else if (matchedProfilId) {
              return <EmployeeProfileDetail profileId={matchedProfilId} navigate={navigate} onShowToast={showToast} />;
            } else {
              return (
                <div className="bg-white border border-gray-200 rounded-xl shadow-sm p-12 text-center max-w-lg mx-auto mt-12">
                  <Clock className="h-12 w-12 text-blue-500 mx-auto mb-4 animate-pulse" />
                  <h2 className="text-lg font-bold text-gray-800">Espace en cours de développement</h2>
                  <p className="text-sm text-gray-500 mt-2">Ce module du SIRH NEXUS est actuellement en cours de finalisation par nos équipes techniques.</p>
                  <button onClick={() => navigate('/tableau-de-bord')} className="mt-6 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg transition-colors">
                    Retourner au tableau de bord
                  </button>
                </div>
              );
            }
          })()}
        </main>

      </div>

      {/* ═══════════════════════════════════════════════════════════════════
          PROFILE DETAIL DRAWER (right panel)
      ════════════════════════════════════════════════════════════════════ */}
      {viewingProfile && (
        <div className="fixed inset-0 z-30 flex">
          {/* Backdrop */}
          <div className="flex-1 bg-black/20" onClick={() => setViewingProfile(null)} />

          {/* Drawer */}
          <div className="w-full max-w-xl bg-white border-l border-gray-200 shadow-xl flex flex-col overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                  <span className="text-sm font-bold text-blue-700">
                    {`${viewingProfile.firstName?.charAt(0) || ''}${viewingProfile.lastName?.charAt(0) || ''}`.toUpperCase()}
                  </span>
                </div>
                <div>
                  <h2 className="text-sm font-semibold text-gray-900">{viewingProfile.firstName} {viewingProfile.lastName}</h2>
                  <p className="text-xs text-gray-500">{viewingProfile.department} · {viewingProfile.jobTitle}</p>
                </div>
              </div>
              <button onClick={() => setViewingProfile(null)} className="p-1.5 rounded hover:bg-gray-100 text-gray-400 transition-colors">
                <X className="h-4 w-4" />
              </button>
            </div>

            {/* Info banner */}
            <div className="px-6 py-3 bg-gray-50 border-b border-gray-100 grid grid-cols-3 gap-4 text-xs">
              <div>
                <p className="text-gray-400 font-medium">E-mail</p>
                <p className="text-gray-800 font-semibold mt-0.5 truncate">{viewingProfile.email}</p>
              </div>
              <div>
                <p className="text-gray-400 font-medium">Matricule</p>
                <p className="text-gray-800 font-semibold mt-0.5">#{viewingProfile.matricule || viewingProfile.userId}</p>
              </div>

            </div>

            {/* Tabs */}
            <div className="flex border-b border-gray-100 px-6">
              {[
                ['infos', 'Informations'],
                ['skills', 'Compétences'],
                ['documents', 'Documents RH']
              ].map(([id, label]) => {
                let badge = '';
                if (id === 'skills') badge = ` (${viewingProfile.skills?.length || 0})`;
                if (id === 'documents') badge = ` (${viewingProfile.documents?.length || 0})`;
                return (
                  <button
                    key={id}
                    onClick={() => setDetailTab(id)}
                    className={`py-3 mr-6 text-xs font-semibold border-b-2 transition-colors
                      ${detailTab === id ? 'border-blue-600 text-blue-700' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
                  >
                    {label}{badge}
                  </button>
                );
              })}
            </div>

            {/* Tab content */}
            <div className="flex-1 overflow-y-auto p-6">
              {detailTab === 'infos' ? (
                <div className="space-y-6">
                  {/* Profil principal résumé */}
                  <div className="bg-gray-50 border border-gray-100 rounded-xl p-4 flex items-center gap-4">
                    <div className="h-14 w-14 rounded-full bg-blue-100 border-2 border-white shadow flex items-center justify-center shrink-0">
                      <span className="text-lg font-bold text-blue-700">
                        {`${viewingProfile.firstName?.charAt(0) || ''}${viewingProfile.lastName?.charAt(0) || ''}`.toUpperCase()}
                      </span>
                    </div>
                    <div>
                      <p className="font-bold text-gray-900 text-sm">{viewingProfile.firstName} {viewingProfile.lastName}</p>
                      <p className="text-xs font-semibold text-blue-600">{viewingProfile.jobTitle}</p>
                      <p className="text-[11px] text-gray-400 mt-0.5">{viewingProfile.department}</p>
                    </div>
                  </div>

                  {/* Bloc Identité */}
                  <div>
                    <h4 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-2.5">Données d'Identité</h4>
                    <div className="grid grid-cols-2 gap-3">
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">CIN (Carte d'Identité)</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">{viewingProfile.cin || 'Non renseigné'}</p>
                      </div>
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Contact Téléphone</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">{viewingProfile.contact || 'Non renseigné'}</p>
                      </div>
                      <div className="col-span-2 bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Adresse de résidence</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">{viewingProfile.adresse || 'Non renseignée'}</p>
                      </div>
                    </div>
                  </div>

                  {/* Bloc Contrat & Compte */}
                  <div>
                    <h4 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-2.5">Contrat & Banque</h4>
                    <div className="grid grid-cols-2 gap-3">
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Type de contrat</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">
                          <span className="inline-flex items-center text-[10px] font-bold text-blue-700 bg-blue-50 px-2 py-0.5 rounded border border-blue-100">
                            {viewingProfile.typeContrat || 'CDI'}
                          </span>
                        </p>
                      </div>
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Date de début</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">
                          {viewingProfile.dateDebutContrat 
                            ? new Date(viewingProfile.dateDebutContrat).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
                            : 'Non définie'}
                        </p>
                      </div>
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Durée de contrat</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">
                          {viewingProfile.dureeContrat ? `${viewingProfile.dureeContrat} mois` : 'Indéterminée (CDI)'}
                        </p>
                      </div>
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">ID Supérieur (Hiérarchie)</p>
                        <p className="text-xs font-semibold text-gray-800 mt-1">
                          {viewingProfile.hierarchieId ? `#${viewingProfile.hierarchieId}` : 'Aucun'}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* Médias & Signature */}
                  <div>
                    <h4 className="text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-2.5">Photo & Empreinte</h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                      {viewingProfile.photoUrl && (
                        <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3 flex items-center gap-3">
                          <img src={viewingProfile.photoUrl} alt="Photo" className="h-8 w-8 rounded-full object-cover border" />
                          <div className="min-w-0">
                            <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Photo URL</p>
                            <p className="text-[10px] text-gray-500 truncate font-mono mt-0.5">{viewingProfile.photoUrl}</p>
                          </div>
                        </div>
                      )}
                      <div className="bg-gray-50/50 border border-gray-100 rounded-lg p-3">
                        <p className="text-[9px] font-bold text-gray-400 uppercase tracking-wider">Signature électronique</p>
                        <p className="text-[10px] font-mono text-gray-500 mt-1 truncate">{viewingProfile.signatureNumerique || 'Non signée'}</p>
                      </div>
                    </div>
                  </div>
                </div>
              ) : detailTab === 'skills' ? (
                (() => {
                  const skillsToDisplay = viewingProfile.skills || [];
                  if (skillsToDisplay.length === 0) {
                    return (
                      <p className="text-xs text-gray-500 italic p-3 text-center bg-gray-50 rounded-lg border border-gray-100">
                        Aucune compétence répertoriée.
                      </p>
                    );
                  }

                  return (
                    <div className="space-y-2">
                      {Array.from(skillsToDisplay).map((s, idx) => (
                        <div key={idx} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg border border-gray-100">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center justify-between mb-1.5">
                              <p className="text-xs font-semibold text-gray-800 truncate">{s.name}</p>
                              <div className="flex items-center gap-2 ml-2 shrink-0">
                                <span className="text-[10px] font-bold text-blue-600 bg-blue-50 border border-blue-100 px-1.5 py-0.5 rounded">
                                  {s.category}
                                </span>
                                <span className="text-[11px] font-semibold text-gray-500">{s.proficiencyLevel}/5</span>
                              </div>
                            </div>
                            <div className="h-1.5 w-full bg-gray-200 rounded-full overflow-hidden">
                              <div
                                className="h-full bg-blue-500 rounded-full transition-all"
                                style={{ width: `${(s.proficiencyLevel / 5) * 100}%` }}
                              />
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  );
                })()
              ) : (
                !viewingProfile.documents?.length ? (
                  <div className="text-center py-12 text-gray-400">
                    <FileText className="h-8 w-8 mx-auto mb-2 text-gray-300" />
                    <p className="text-xs font-medium">Aucun document dans le coffre-fort</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {Array.from(viewingProfile.documents).map((doc, idx) => (
                      <div key={idx} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-100">
                        <div className="flex items-center gap-3 min-w-0">
                          <div className="p-2 rounded-lg bg-white border border-gray-200 shrink-0">
                            <FileText className="h-4 w-4 text-gray-500" />
                          </div>
                          <div className="min-w-0">
                            <p className="text-xs font-semibold text-gray-800">{doc.documentType}</p>
                            <p className="text-[10px] text-gray-400 font-mono truncate">{doc.filePath}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2 ml-2 shrink-0">
                          {doc.isSigned ? (
                            <span className="inline-flex items-center gap-1 text-[10px] font-semibold text-green-700 bg-green-50 border border-green-200 px-1.5 py-0.5 rounded">
                              <CheckCircle className="h-3 w-3" />
                              Signé
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 text-[10px] font-semibold text-amber-700 bg-amber-50 border border-amber-200 px-1.5 py-0.5 rounded">
                              <Clock className="h-3 w-3" />
                              En attente
                            </span>
                          )}
                          <span className="text-[10px] text-gray-400">
                            {new Date(doc.uploadDate).toLocaleDateString('fr-FR')}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )
              )}
            </div>

            {/* Drawer footer actions */}
            {hasAdminRole() && (
              <div className="px-6 py-3 border-t border-gray-100 flex gap-2">
                <button
                  onClick={e => { handleOpenEdit(viewingProfile, e); setViewingProfile(null); }}
                  className="flex-1 flex items-center justify-center gap-1.5 text-xs font-semibold border border-gray-200 hover:border-blue-300 hover:text-blue-700 text-gray-600 py-2 rounded-lg transition-colors"
                >
                  <Pencil className="h-3.5 w-3.5" />
                  Modifier
                </button>
                <button
                  onClick={e => handleDeleteProfile(viewingProfile.id, e)}
                  className="flex items-center justify-center gap-1.5 text-xs font-semibold border border-gray-200 hover:border-red-200 hover:text-red-600 text-gray-500 px-4 py-2 rounded-lg transition-colors"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════════════════════════════
          CREATE / EDIT MODAL
      ════════════════════════════════════════════════════════════════════ */}
      {isFormOpen && (
        <div className="fixed inset-0 z-40 flex items-center justify-center p-4 bg-black/25">
          <div className="w-full max-w-2xl bg-white rounded-xl border border-gray-200 shadow-xl overflow-hidden flex flex-col">
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 shrink-0">
              <div>
                <h2 className="text-sm font-semibold text-gray-900">
                  {editingProfile ? 'Modifier le profil' : 'Nouveau profil employé'}
                </h2>
                <p className="text-xs text-gray-400 mt-0.5">
                  {editingProfile ? `ID profil #${editingProfile.id}` : 'Créer un nouveau collaborateur'}
                </p>
              </div>
              <button onClick={handleCloseForm} className="p-1.5 rounded hover:bg-gray-100 text-gray-400 transition-colors">
                <X className="h-4 w-4" />
              </button>
            </div>

            {/* Form */}
            <form onSubmit={handleSaveProfile} className="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
              
              {/* SECTION 1: INFORMATIONS PROFESSIONNELLES */}
              <div>
                <h3 className="text-xs font-bold text-blue-600 uppercase tracking-wider mb-3 pb-1 border-b border-gray-100">
                  1. Informations Professionnelles
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">
                      Identifiant utilisateur système <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="number"
                      disabled={!!editingProfile}
                      value={formUserId}
                      onChange={e => setFormUserId(e.target.value)}
                      placeholder="Ex: 2 (compte Jane Doe)"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400 disabled:bg-gray-50 disabled:text-gray-400"
                      required
                    />
                    {!editingProfile && (
                      <p className="text-[10px] text-gray-400 mt-1">
                        Saisissez <strong>2</strong> pour Jane Doe ou <strong>1</strong> pour Admin.
                      </p>
                    )}
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">
                      Matricule <span className="text-gray-400">(Auto-généré si vide)</span>
                    </label>
                    <input
                      type="text"
                      value={formMatricule}
                      onChange={e => setFormMatricule(e.target.value)}
                      placeholder="Ex: E0014"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">
                      Manager Direct (Hiérarchie ID)
                    </label>
                    <input
                      type="number"
                      value={formHierarchieId}
                      onChange={e => setFormHierarchieId(e.target.value)}
                      placeholder="Ex: 1 (Admin)"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">
                      Intitulé du poste <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formJobTitle}
                      onChange={e => setFormJobTitle(e.target.value)}
                      placeholder="Ex: Développeur Backend"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">
                      Département <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formDept}
                      onChange={e => setFormDept(e.target.value)}
                      placeholder="Ex: R&D, IT, RH..."
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                      required
                    />
                  </div>
                </div>
              </div>

              {/* SECTION 2: INFORMATIONS PERSONNELLES */}
              <div>
                <h3 className="text-xs font-bold text-blue-600 uppercase tracking-wider mb-3 pb-1 border-b border-gray-100">
                  2. Informations Personnelles (Identité)
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">CIN (Carte Nationale d'Identité)</label>
                    <input
                      type="text"
                      value={formCin}
                      onChange={e => setFormCin(e.target.value)}
                      placeholder="Ex: AB123456"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">Contact (Téléphone)</label>
                    <input
                      type="text"
                      value={formContact}
                      onChange={e => setFormContact(e.target.value)}
                      placeholder="Ex: +212 600000000"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">Adresse de résidence</label>
                    <input
                      type="text"
                      value={formAdresse}
                      onChange={e => setFormAdresse(e.target.value)}
                      placeholder="Ex: Quartier Smaïla, Settat, Maroc"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                </div>
              </div>

              {/* SECTION 3: CONTRAT & BANQUE */}
              <div>
                <h3 className="text-xs font-bold text-blue-600 uppercase tracking-wider mb-3 pb-1 border-b border-gray-100">
                  3. Contrat & Coordonnées Bancaires
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">Type de contrat</label>
                    <select
                      value={formTypeContrat}
                      onChange={e => setFormTypeContrat(e.target.value)}
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                    >
                      <option value="CDI">CDI (Indéterminé)</option>
                      <option value="CDD">CDD (Déterminé)</option>
                      <option value="Stage">Stage</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">Date de début de contrat</label>
                    <input
                      type="date"
                      value={formDateDebutContrat}
                      onChange={e => setFormDateDebutContrat(e.target.value)}
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">Durée du contrat (en mois)</label>
                    <input
                      type="number"
                      value={formDureeContrat}
                      onChange={e => setFormDureeContrat(e.target.value)}
                      placeholder="Ex: 6 (si CDD/Stage, laisser vide si CDI)"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">RIB (Coordonnées Bancaires - AES-256)</label>
                    <input
                      type="text"
                      value={formRib}
                      onChange={e => setFormRib(e.target.value)}
                      placeholder="FR76 3000 2000 1000 2345 6789 123"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400 font-mono"
                    />
                  </div>
                </div>
              </div>

              {/* SECTION 4: MÉDIAS ET SIGNATURE */}
              <div>
                <h3 className="text-xs font-bold text-blue-600 uppercase tracking-wider mb-3 pb-1 border-b border-gray-100">
                  4. Photo & Signature Numérique
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">URL de la photo de profil</label>
                    <input
                      type="text"
                      value={formPhotoUrl}
                      onChange={e => setFormPhotoUrl(e.target.value)}
                      placeholder="Ex: https://storage.nexus.com/profiles/photo.jpg"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1.5">Signature électronique (Empreinte/Hash)</label>
                    <input
                      type="text"
                      value={formSignatureNumerique}
                      onChange={e => setFormSignatureNumerique(e.target.value)}
                      placeholder="Ex: hash_signature_abc123"
                      className="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-400 font-mono"
                    />
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-4 border-t border-gray-100 shrink-0">
                <button
                  type="button"
                  onClick={handleCloseForm}
                  className="flex-1 text-xs font-semibold border border-gray-200 hover:bg-gray-50 text-gray-600 py-2.5 rounded-lg transition-colors"
                >
                  Annuler
                </button>
                <button
                  type="submit"
                  disabled={formLoading}
                  className="flex-1 text-xs font-semibold bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white py-2.5 rounded-lg transition-colors flex items-center justify-center gap-1.5"
                >
                  {formLoading
                    ? <span className="h-4 w-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    : (editingProfile ? 'Enregistrer les modifications' : 'Créer le profil')}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      <SmartAssistantWidget />
    </div>
  );
}

export default App;
