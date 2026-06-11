import React, { useState } from 'react';
import {
  ShieldCheck,
  CloudUpload,
  Download,
  Trash2,
  FileText,
  FileCheck,
  Search,
  Lock,
  Plus
} from 'lucide-react';

// ---------------------------------------------------------------------------
// Mock Data (Initial Documents) - Purged
// ---------------------------------------------------------------------------
const INITIAL_DOCUMENTS = [];

const CATEGORY_STYLES = {
  Contrat: 'bg-blue-50 text-blue-700 border-blue-100',
  Paie: 'bg-emerald-50 text-emerald-700 border-emerald-100',
  Administratif: 'bg-indigo-50 text-indigo-700 border-indigo-100',
};

// ---------------------------------------------------------------------------
// Main Component
// ---------------------------------------------------------------------------
export default function DocumentsVaultView({ onShowToast }) {
  const [documents, setDocuments] = useState(INITIAL_DOCUMENTS);
  const [searchQuery, setSearchQuery] = useState('');

  const triggerToast = (message, type = 'success') => {
    if (onShowToast) {
      onShowToast(message, type);
    } else {
      console.log(`[Toast ${type}]: ${message}`);
    }
  };

  const handleDelete = (id, name) => {
    if (window.confirm(`Voulez-vous vraiment supprimer définitivement le document "${name}" ?`)) {
      setDocuments(prev => prev.filter(doc => doc.id !== id));
      triggerToast(`Document "${name}" supprimé définitivement du coffre-fort.`, 'success');
    }
  };

  const handleDownload = (name) => {
    triggerToast(`Téléchargement de "${name}" démarré.`, 'success');
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = (e) => {
    e.preventDefault();
    triggerToast("Importation réussie. (Simulation d'upload dans le coffre-fort)", 'success');
  };

  const filteredDocs = documents.filter(doc =>
    doc.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    doc.category.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="w-full min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/20 to-indigo-50/30 p-4 md:p-6 lg:p-8">
      
      {/* Header */}
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1.5">
            <div className="p-2.5 bg-blue-600 text-white rounded-2xl shadow-md shadow-blue-200">
              <Lock className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-xl font-extrabold text-gray-900 tracking-tight">
                Coffre-fort Numérique
              </h1>
              <p className="text-xs text-gray-400 font-medium mt-0.5 flex items-center gap-1">
                <ShieldCheck className="h-3.5 w-3.5 text-emerald-500" />
                Espace hautement sécurisé et crypté (Norme AES-256)
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Upload Area */}
      <div className="mb-8">
        <div
          onDragOver={handleDragOver}
          onDrop={handleDrop}
          className="w-full bg-white/70 backdrop-blur-md border-2 border-dashed border-gray-300 hover:border-blue-400 hover:bg-blue-50/30 transition-all duration-200 rounded-3xl p-8 flex flex-col items-center justify-center text-center cursor-pointer group"
          onClick={() => triggerToast("Sélection de fichier ouverte. (Simulation)", 'info')}
        >
          <div className="p-4 bg-blue-50 text-blue-600 rounded-2xl mb-4 group-hover:scale-110 transition-transform duration-200">
            <CloudUpload className="h-6 w-6" />
          </div>
          <h3 className="text-sm font-bold text-gray-800">
            Glissez-déposez un document ici ou cliquez pour parcourir
          </h3>
          <p className="text-xs text-gray-400 mt-1 max-w-xs font-medium">
            Formats acceptés : PDF, PNG, JPG, DOCX (Max 10 Mo par fichier)
          </p>
          <button
            type="button"
            className="mt-4 inline-flex items-center gap-1.5 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-bold text-xs rounded-xl shadow-md shadow-blue-200 transition-all active:scale-95"
          >
            <Plus className="h-3.5 w-3.5" />
            Importer un document
          </button>
        </div>
      </div>

      {/* Main Table Container */}
      <div className="bg-white/90 backdrop-blur-lg border border-gray-200/80 rounded-3xl shadow-xl overflow-hidden">
        
        {/* Table Title Bar */}
        <div className="px-6 py-5 border-b border-gray-100 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <h2 className="text-sm font-bold text-gray-900">Documents Disponibles</h2>
            <p className="text-[11px] text-gray-400 font-medium mt-0.5">
              Consultez et téléchargez vos documents administratifs
            </p>
          </div>

          {/* Search bar inside toolbar */}
          <div className="relative w-full sm:w-64">
            <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="Rechercher un document..."
              className="w-full pl-9 pr-3 py-2 text-xs border border-gray-200 rounded-xl outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-500/10 transition-all bg-gray-50/50"
            />
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100">
                <th className="py-3.5 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                  Nom du document
                </th>
                <th className="py-3.5 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                  Catégorie
                </th>
                <th className="py-3.5 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                  Date d'ajout
                </th>
                <th className="py-3.5 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                  Statut
                </th>
                <th className="py-3.5 px-6 text-[10px] font-bold text-gray-400 uppercase tracking-wider text-right">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan="5" className="py-12 text-center text-gray-400">
                    <FileText className="h-8 w-8 mx-auto mb-2 text-gray-300" />
                    Aucun document trouvé.
                  </td>
                </tr>
              ) : (
                filteredDocs.map(doc => {
                  const isSigned = doc.status === 'SIGNED';

                  return (
                    <tr key={doc.id} className="hover:bg-slate-50/40 transition-colors">
                      {/* Nom du document */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div className="h-9 w-9 rounded-2xl bg-blue-50 border border-blue-100 flex items-center justify-center shrink-0 shadow-sm">
                            <FileText className="h-5 w-5 text-blue-600" />
                          </div>
                          <div>
                            <p className="font-bold text-gray-900 text-xs">{doc.name}</p>
                            <p className="text-[10px] text-gray-400 font-medium">{doc.size}</p>
                          </div>
                        </div>
                      </td>

                      {/* Catégorie */}
                      <td className="py-4 px-6">
                        <span className={`inline-flex items-center text-[10px] font-bold px-2.5 py-1 rounded-xl border ${CATEGORY_STYLES[doc.category] || 'bg-gray-50 text-gray-600'}`}>
                          {doc.category}
                        </span>
                      </td>

                      {/* Date d'ajout */}
                      <td className="py-4 px-6">
                        <span className="text-xs text-gray-600 font-semibold">
                          {new Date(doc.uploadedAt).toLocaleDateString('fr-FR', {
                            day: '2-digit',
                            month: 'short',
                            year: 'numeric',
                          })}
                        </span>
                      </td>

                      {/* Statut */}
                      <td className="py-4 px-6">
                        {isSigned ? (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-100">
                            <FileCheck className="h-3.5 w-3.5 shrink-0" />
                            Signé
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-xl text-xs font-bold bg-gray-50 text-gray-500 border border-gray-200">
                            À signer
                          </span>
                        )}
                      </td>

                      {/* Actions */}
                      <td className="py-4 px-6">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleDownload(doc.name)}
                            className="p-1.5 rounded-xl hover:bg-blue-50 text-gray-400 hover:text-blue-600 transition-colors border border-transparent hover:border-blue-100 shadow-sm hover:shadow active:scale-95"
                            title="Télécharger"
                          >
                            <Download className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleDelete(doc.id, doc.name)}
                            className="p-1.5 rounded-xl hover:bg-rose-50 text-gray-400 hover:text-rose-600 transition-colors border border-transparent hover:border-rose-100 shadow-sm hover:shadow active:scale-95"
                            title="Supprimer"
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
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
