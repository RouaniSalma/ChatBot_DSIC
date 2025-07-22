'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './Dashboard.module.css';

// Interface alignée avec l'entité Java TypeEvenement
interface TypeEvenement {
  idType: number;
  typeEvent: string;
}

// Interface alignée avec l'entité Java Evenement
interface Evenement {
  idEvenement?: number;
  titre: string;
  description: string;
  capaciteMax: number;
  lieu: string;
  dateDebut: string;
  dateFin: string;
  type: { idType: number };
  statut: string;
  // Les autres champs comme 'utilisateur' et 'participants' ne sont pas nécessaires pour le formulaire.
}
interface PaginatedResponse {
  evenements: Evenement[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
}
interface FilterState {
  typeId?: number | null;
}

const STATUTS = [
  { value: 'PROCHAIN', label: 'Prochain' },
  { value: 'EN_COURS', label: 'En cours' },
  { value: 'TERMINE', label: 'Terminé' },
];

export default function Dashboard() {
  console.log("Dashboard component rendu");
  const [filter, setFilter] = useState<FilterState>({ typeId: null });
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalItems: 0,
    totalPages: 0,
    itemsPerPage: 4,
  });
  const [evenements, setEvenements] = useState<Evenement[]>([]);
  const [types, setTypes] = useState<TypeEvenement[]>([]); // État pour les types d'événements
  useEffect(() => {
  console.log("Filtre changé - typeId:", filter.typeId);
  refresh(0); // Toujours rafraîchir à la première page quand le filtre change
}, [filter.typeId]); // Dépendance uniquement sur typeId
  useEffect(() => {
  console.log("Current filter:", filter);
  console.log("Current events:", evenements);
}, [filter, evenements]);
  const [modal, setModal] = useState<'create' | 'edit' | 'details' | null>(null);
  const [selected, setSelected] = useState<Evenement | null>(null);
  // État du formulaire corrigé pour correspondre à l'entité Evenement
  const [form, setForm] = useState<Evenement>({
    titre: '',
    description: '',
    capaciteMax: 0,
    lieu: '',
    dateDebut: '',
    dateFin: '',
    type: { idType: 0 },
    statut: 'PROCHAIN',
  });
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const [errors, setErrors] = useState({
    titre: '',
    description: '',
    capaciteMax: '',
    lieu: '',
  });

  const MAX_TITRE_LENGTH = 100;

  // Fonctions de validation
  const validateText = (value: string) => {
    // Autorise lettres, espaces, accents, tirets, apostrophes
    return /^[A-Za-zÀ-ÖØ-öø-ÿ '\-]*$/.test(value);
  };
  const validateCapacite = (value: number) => value > 0;

  // Gestion des changements avec validation
  const handleChange = (field: keyof Evenement, value: any) => {
    let error = '';
    if (field === 'titre') {
      if (!validateText(value)) {
        error = 'Seules les lettres et espaces sont autorisées';
      } else if (value.length > MAX_TITRE_LENGTH) {
        error = `Le titre ne doit pas dépasser ${MAX_TITRE_LENGTH} caractères`;
      }
    }
    if (field === 'lieu') {
      if (!validateText(value)) {
        error = 'Seules les lettres et espaces sont autorisées';
      }
    }
    if (field === 'description') {
      if (value.length > 0 && !validateText(value)) {
        error = 'Seules les lettres et espaces sont autorisées';
      }
    }
    if (field === 'capaciteMax') {
      // Empêche les zéros en début et force un nombre strictement positif
      let valStr = String(value).replace(/^0+/, '');
      if (valStr === '') valStr = '1';
      const valNum = Number(valStr);
      if (!validateCapacite(valNum)) {
        error = 'La capacité doit être strictement positive';
      }
      setForm(f => ({ ...f, [field]: valNum }));
      setErrors(prev => ({ ...prev, [field]: error }));
      return;
    }
    setErrors(prev => ({ ...prev, [field]: error }));
    setForm(f => ({ ...f, [field]: value }));
  };

  // Fetch des événements et des types
  const refresh = async (page = pagination.currentPage) => {
  const token = localStorage.getItem('token');
  if (!token) return;

  try {
    // Construction robuste de l'URL
    const params = new URLSearchParams({
      page: page.toString(),
      size: pagination.itemsPerPage.toString()
    });
    
    // Ajout conditionnel du typeId
    if (filter.typeId !== undefined && filter.typeId !== null) {
      params.append('typeId', filter.typeId.toString());
    }

    const url = `http://localhost:8081/api/evenements/filter?${params.toString()}`;
    console.log("URL envoyée:", url); // Vérifiez cette ligne !

    const [resEvents, resTypes] = await Promise.all([
      fetch(url, { 
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }),
      fetch('http://localhost:8081/api/types-evenement', { 
        headers: { 'Authorization': `Bearer ${token}` } 
      })
    ]);

    // 3. Traitement de la réponse des événements
    if (resEvents.ok) {
      const data: PaginatedResponse = await resEvents.json();
      console.log("Données des événements reçues:", data);
      
      setEvenements(data.evenements || []);
      setPagination({
        ...pagination,
        currentPage: data.currentPage || 0,
        totalItems: data.totalItems || 0,
        totalPages: data.totalPages || 0,
      });
    } else {
      const errorText = await resEvents.text();
      console.error("Erreur événements:", resEvents.status, errorText);
    }

    // 4. Traitement de la réponse des types
    if (resTypes.ok) {
      const typesData = await resTypes.json();
      console.log("Types d'événements reçus:", typesData);
      setTypes(typesData);
    } else {
      const errorText = await resTypes.text();
      console.error("Erreur types:", resTypes.status, errorText);
    }

  } catch (error) {
    console.error("Erreur lors du chargement des données:", error);
  }
};
  useEffect(() => {
    const token = localStorage.getItem('token');
     console.log("useEffect token:", token);
    if (!token) {
      router.push('/login');
      return;
    }
    /*refresh();*/

    // Rafraîchissement automatique toutes les 30 secondes
    const interval = setInterval(() => {
      refresh(pagination.currentPage); 
    }, 30000); // 30 000 ms = 30 secondes
    return () => clearInterval(interval);
  }, [router, filter.typeId, pagination.currentPage]);

  function getUserIdFromToken() {
    const token = localStorage.getItem('token');
    if (!token) return null;
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub; // c'est l'email
  }

  const handleCreate = async () => {
    console.log('handleCreate appelé');
    setLoading(true);
    const utilisateurId = localStorage.getItem('idUtilisateur');
    if (!utilisateurId) {
      alert("Utilisateur non authentifié !");
      setLoading(false);
      return;
    }

    const token = localStorage.getItem('token');

    // Formatage des dates (s'assure qu'il y a les secondes)
    const formatDate = (d: string) => d.length === 16 ? `${d}:00` : d.substring(0, 19);

    const evenementToSend = {
      ...form,
      dateDebut: formatDate(form.dateDebut),
      dateFin: formatDate(form.dateFin),
      type: { idType: Number(form.type.idType) } // Assurez-vous que c'est un nombre
    };

    const res = await fetch(`http://localhost:8081/api/evenements?utilisateurId=${utilisateurId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(evenementToSend),
    });
    console.log('Status:', res.status);
    if (!res.ok) {
      const errorText = await res.text();
      alert('Erreur lors de la création : ' + errorText);
      setLoading(false);
      return;
    }
    setModal(null);
    setLoading(false);
    refresh(); // Rafraîchit les données
  };

  const handleEdit = async () => {
    if (!selected?.idEvenement) return;
    setLoading(true);
    const token = localStorage.getItem('token');

    const formatDate = (d: string) => d.length === 16 ? `${d}:00` : d.substring(0, 19);

    const evenementToSend = {
      ...form,
      dateDebut: formatDate(form.dateDebut),
      dateFin: formatDate(form.dateFin),
      type: { idType: Number(form.type.idType) }
    };

    await fetch(`http://localhost:8081/api/evenements/${selected.idEvenement}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(evenementToSend),
    });
    setModal(null);
    setSelected(null);
    setLoading(false);
    refresh();
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Supprimer cet événement ?')) return;
    const token = localStorage.getItem('token');
    await fetch(`http://localhost:8081/api/evenements/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` },
    });
    refresh();
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/login');
  };

  // Helpers pour ouvrir les modales
  const openCreate = () => {
    // Réinitialise le formulaire avec des valeurs par défaut
    setForm({
      titre: '',
      description: '',
      capaciteMax: 0,
      lieu: '',
      dateDebut: '',
      dateFin: '',
      type: { idType: 0 },
      statut: 'PROCHAIN',
    });
    setErrors({
      titre: '',
      description: '',
      capaciteMax: '',
      lieu: '',
    });
    setModal('create');
  };

  const openEdit = (ev: Evenement) => {
    setSelected(ev);
    // Prépare les dates pour les champs datetime-local
    const formatForInput = (dateStr: string) => dateStr ? dateStr.substring(0, 16) : '';
    setForm({
      ...ev,
      type: ev.type || { idType: '' },
      dateDebut: formatForInput(ev.dateDebut),
      dateFin: formatForInput(ev.dateFin),
    });
    setErrors({
      titre: '',
      description: '',
      capaciteMax: '',
      lieu: '',
    });
    setModal('edit');
  };

  const openDetails = (ev: Evenement) => {
    setSelected(ev);
    setModal('details');
  };
 console.log('types:', types);
  return (
    <div className={styles.container}>
      {/* Header */}
      <header className={styles.header}>
        <img src="/logo-maroc.png" alt="Logo" className={styles.logo} />
        <button className={styles.logoutBtn} onClick={handleLogout}>Déconnexion</button>
      </header>

      {/* Titre + bouton ajouter */}
      <div className={styles.topBar}>
        <h1 className={styles.title}>Liste des événements</h1>
        <button className={styles.addBtn} onClick={openCreate}>+ Ajouter événement</button>
      </div>
      {/* Nouveau bloc: Filtre et pagination */}
    <div className={styles.filterBar}>
  <div className={styles.filterGroup}>
    <label>Filtrer par type :</label>
    <select
      value={filter.typeId ?? ''}
      onChange={(e) => {
        const typeId = e.target.value ? parseInt(e.target.value) : null;
        setFilter({ typeId });
        setPagination(prev => ({ ...prev, currentPage: 0 }));
      }}
    >
      <option value="">Tous les types</option>
      {types.map((type) => (
        <option key={type.idType} value={type.idType}>
          {type.typeEvent}
        </option>
      ))}
    </select>
  </div>
</div>
      
      
      {/* Tableau des événements corrigé */}
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Titre</th>
              <th>Date début</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {evenements.map(ev => (
              <tr key={ev.idEvenement}>
                <td>{ev.idEvenement}</td>
                <td>{ev.titre}</td>
                <td>{new Date(ev.dateDebut).toLocaleString()}</td>
                <td>
                  <span className={`${styles.status} ${ev.statut ? styles[ev.statut.toLowerCase()] : ''}`}>
                    {STATUTS.find(s => s.value === ev.statut)?.label || ev.statut || 'N/A'}
                  </span>
                </td>
                <td>
                  <button className={styles.actionBtn} title="Voir détails" onClick={() => openDetails(ev)}>👁️</button>
                  <button className={styles.actionBtn} title="Modifier" onClick={() => openEdit(ev)}>✏️</button>
                  <button className={styles.actionBtn} title="Supprimer" onClick={() => handleDelete(ev.idEvenement!)}>🗑️</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
{/* Pagination en bas */}
<div className={styles.paginationContainer}>
  <div className={styles.pagination}>
    <button
      onClick={() => refresh(pagination.currentPage - 1)}
      disabled={pagination.currentPage === 0}
    >
      Précédent
    </button>
    
    <span>
      Page {pagination.currentPage + 1} sur {pagination.totalPages}
    </span>
    
    <button
      onClick={() => refresh(pagination.currentPage + 1)}
      disabled={pagination.currentPage >= pagination.totalPages - 1}
    >
      Suivant
    </button>
  </div>
</div>
      {/* Modals */}
      {(modal === 'create' || modal === 'edit') && (
  <div className={styles.modalOverlay}>
    <div className={`${styles.modal} ${styles.wideModal}`}>
      <h2>{modal === 'create' ? 'Ajouter un événement' : 'Modifier un événement'}</h2>
      <div className={styles.formGrid}>
        <div className={styles.formColumn}>
          <div className={styles.formGroup}>
            <label>Titre</label>
            <input
              value={form.titre}
              onChange={e => handleChange('titre', e.target.value)}
              required
              maxLength={MAX_TITRE_LENGTH}
            />
            {errors.titre && <span style={{ color: 'red', fontSize: '0.9em' }}>{errors.titre}</span>}
          </div>
          <div className={styles.formGroup}>
            <label>Description</label>
            <textarea
              value={form.description}
              onChange={e => handleChange('description', e.target.value)}
              required
              rows={4}
            />
            {errors.description && <span style={{ color: 'red', fontSize: '0.9em' }}>{errors.description}</span>}
          </div>
          <div className={styles.formGroup}>
            <label>Capacité max</label>
            <input
              type="number"
              value={form.capaciteMax === 0 ? '' : String(form.capaciteMax)}
              onChange={e => {
                // Nettoie la valeur pour enlever les zéros initiaux
                let valStr = e.target.value.replace(/^0+/, '');
                if (valStr === '') valStr = '1';
                handleChange('capaciteMax', valStr);
              }}
              required
              min={1}
              inputMode="numeric"
            />
            {errors.capaciteMax && <span style={{ color: 'red', fontSize: '0.9em' }}>{errors.capaciteMax}</span>}
          </div>
        </div>
        <div className={styles.formColumn}>
          <div className={styles.formGroup}>
            <label>Type d'événement</label>
            <select
              value={form.type?.idType || ''}
              onChange={e => setForm(f => ({
                ...f,
                type: { ...f.type, idType: Number(e.target.value) }
              }))}
              required
            >
              <option value="">-- Sélectionner --</option>
              {types.map(t => (
                <option key={t.idType} value={t.idType}>
                  {t.typeEvent}
                </option>
              ))}
            </select>
          </div>
          <div className={styles.formGroup}>
            <label>Date de début</label>
            <input
              type="datetime-local"
              value={form.dateDebut}
              onChange={e => setForm(f => ({ ...f, dateDebut: e.target.value }))}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Date de fin</label>
            <input
              type="datetime-local"
              value={form.dateFin}
              onChange={e => setForm(f => ({ ...f, dateFin: e.target.value }))}
              required
            />
          </div>
          <div className={styles.formGroup}>
            <label>Lieu</label>
            <input
              value={form.lieu}
              onChange={e => handleChange('lieu', e.target.value)}
              required
            />
            {errors.lieu && <span style={{ color: 'red', fontSize: '0.9em' }}>{errors.lieu}</span>}
          </div>
        </div>
      </div>
      <div className={styles.modalActions}>
        <button className={`${styles.modalBtn} ${styles.secondary}`} onClick={() => setModal(null)} type="button">Annuler</button>
        <button
  className={`${styles.modalBtn} ${styles.primary}`}
  onClick={modal === 'create' ? handleCreate : handleEdit}
  type="button"
  disabled={
    loading ||
    !form.type.idType ||
    !!errors.titre ||
    !!errors.description ||
    !!errors.capaciteMax ||
    !!errors.lieu ||
    !form.titre.trim() ||
    !form.description.trim() ||
    !form.lieu.trim() ||
    !validateCapacite(form.capaciteMax)
  }
>
  {modal === 'create' ? 'Créer' : 'Enregistrer'}
</button>
      </div>
    </div>
  </div>
  
)}

      {modal === 'details' && selected && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2>Détails de l'événement</h2>
            <p><b>ID :</b> {selected.idEvenement}</p>
            <p><b>Titre :</b> {selected.titre}</p>
            <p><b>Description :</b> {selected.description}</p>
            <p><b>Capacité max :</b> {selected.capaciteMax}</p>
            <p><b>Type :</b> {types.find(t => t.idType === selected.type?.idType)?.typeEvent || 'N/A'}</p>
            <p><b>Date début :</b> {new Date(selected.dateDebut).toLocaleString()}</p>
            <p><b>Date fin :</b> {new Date(selected.dateFin).toLocaleString()}</p>
            <p><b>Lieu :</b> {selected.lieu}</p>
            <p><b>Statut :</b> {STATUTS.find(s => s.value === selected.statut)?.label || selected.statut}</p>
            <div className={styles.modalActions}>
              <button className={`${styles.modalBtn} ${styles.primary}`} onClick={() => setModal(null)}>Fermer</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}