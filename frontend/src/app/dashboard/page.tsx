'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { FrenchDateTimePicker } from './FrenchDateTimePicker'
import { format } from 'date-fns';
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
interface Notification {
  message: string;
  type: 'success' | 'error' | 'info';
  id: number;
}

const STATUTS = [
  { value: 'PROCHAIN', label: 'À venir' },
  { value: 'EN_COURS', label: 'En cours' },
  { value: 'TERMINE', label: 'Passé' },
];

export default function Dashboard() {
  console.log("Dashboard component rendu");
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [filter, setFilter] = useState<FilterState>({ typeId: null });
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalItems: 0,
    totalPages: 0,
    itemsPerPage: 4,
  });
  const [evenements, setEvenements] = useState<Evenement[]>([]);
  const [types, setTypes] = useState<TypeEvenement[]>([]); // État pour les types d'événements
  // Ajoutez cet effet pour afficher la notification de bienvenue
useEffect(() => {
  const welcomeFlag = localStorage.getItem('welcomeNotification');
  const nom = localStorage.getItem('nomUtilisateur');
  const prenom = localStorage.getItem('prenomUtilisateur');

  if (welcomeFlag && nom && prenom) {
    addNotification(`Bienvenue ${prenom} ${nom} !`, 'success');
    // Supprimez le flag pour ne pas afficher à nouveau
    localStorage.removeItem('welcomeNotification');
  }
}, []); // Exécuté une seule fois au montage
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
// Fonction d'affichage
// Fonction d'affichage corrigée pour le fuseau horaire
const formatDisplayDate = (isoString: string) => {
  if (!isoString) return '';
  
  try {
    // Parse en considérant le décalage UTC+1
    const date = new Date(isoString);
    return date.toLocaleString('fr-FR', {
      timeZone: 'Africa/Casablanca',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  } catch (error) {
    console.error('Erreur de formatage:', error);
    return isoString;
  }
};
  const MAX_TITRE_LENGTH = 100;
const addNotification = (message: string, type: 'success' | 'error' | 'info') => {
  const id = Date.now();
  setNotifications(prev => [...prev, { message, type, id }]);
  
  // Suppression automatique après 5 secondes
  setTimeout(() => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  }, 5000);
};
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
  setLoading(true);
  
  // Validation requise
  if (!form.titre || !form.lieu || !form.type.idType) {
    addNotification("Veuillez remplir tous les champs obligatoires", 'error');
    setLoading(false);
    return;
  }
  //  LA VALIDATION DES DATES (NOUVEAU CODE)
  const startDate = new Date(form.dateDebut);
  const endDate = new Date(form.dateFin);
  
  if (endDate <= startDate) {
    addNotification("La date de fin doit être postérieure à la date de début", 'error');
    setLoading(false);
    return;
  }
  try {
    // Conversion ISO 8601 -> format backend
  const formatDateForBackend = (dateString: string) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  // Compense le décalage du fuseau horaire
  const timezoneOffset = date.getTimezoneOffset() * 60000;
  const localDate = new Date(date.getTime() - timezoneOffset);
  
  return localDate.toISOString().slice(0, 19); // "YYYY-MM-DDTHH:mm:ss"
};
     const toLocalISO = (dateString: string) => {
      const date = new Date(dateString);
      const offset = date.getTimezoneOffset() * 60000;
      return new Date(date.getTime() - offset).toISOString().slice(0, 19);
    };
    const payload = {
      ...form,
      dateDebut: form.dateDebut,
      dateFin: form.dateFin,
      type: { idType: form.type.idType } // Conversion numérique déjà faite
    };
    console.log('Dates envoyées:', {
      début: payload.dateDebut,
      fin: payload.dateFin
    });
    console.log('Payload envoyé:', JSON.stringify(payload, null, 2));

    const response = await fetch(`http://localhost:8081/api/evenements?utilisateurId=${localStorage.getItem('idUtilisateur')}`, {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      const error = await response.text();
      throw new Error(error || "Erreur serveur");
    }

    addNotification("Événement créé avec succès", 'success');
    setModal(null);
    refresh();
  } catch (error) {
    console.error('Erreur création:', error);
    addNotification(
      `Échec de la création: ${error instanceof Error ? error.message : 'Erreur inconnue'}`,
      'error'
    );
  } finally {
    
    setLoading(false);
  }
};

  const handleEdit = async () => {
  if (!selected?.idEvenement) {
    addNotification("Aucun événement sélectionné !", 'error');
    return;
  }

  setLoading(true);
  const token = localStorage.getItem('token');
  
  if (!token) {
    addNotification("Token d'authentification manquant !", 'error');
    setLoading(false);
    return;
  }

  // Validation des champs obligatoires
  if (!form.titre || !form.lieu || !form.type.idType) {
    addNotification("Veuillez remplir tous les champs obligatoires !", 'error');
    setLoading(false);
    return;
  }

  // Validation des dates
  if (new Date(form.dateFin) <= new Date(form.dateDebut)) {
    addNotification("La date de fin doit être postérieure à la date de début !", 'error');
    setLoading(false);
    return;
  }

  // Fonction pour formater les dates pour le backend
  const formatDateForBackend = (isoString: string) => {
    if (!isoString) return '';
    // Garde seulement les 19 premiers caractères (supprime le timezone)
    return isoString.substring(0, 19);
  };

  try {
    const evenementToSend = {
      ...form,
      dateDebut: formatDateForBackend(form.dateDebut),
      dateFin: formatDateForBackend(form.dateFin),
      type: { idType: Number(form.type.idType) }
    };
    // AJOUTEZ ICI LE CONSOLE.LOG
    console.log('Debug Timezone:', {
  selection: form.dateDebut,
  asDate: new Date(form.dateDebut),
  getTimezoneOffset: new Date(form.dateDebut).getTimezoneOffset(),
  utcString: new Date(form.dateDebut).toUTCString()
});
    console.log('Données envoyées:', evenementToSend); // Pour débogage

    const response = await fetch(`http://localhost:8081/api/evenements/${selected.idEvenement}`, {
      method: 'PUT',
      headers: { 
        'Content-Type': 'application/json', 
        'Authorization': `Bearer ${token}` 
      },
      body: JSON.stringify(evenementToSend),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Erreur lors de la modification");
    }

    // Gestion de la réponse vide
    const text = await response.text();
    const updatedEvent = text ? JSON.parse(text) : { titre: form.titre };

    addNotification(`Événement "${updatedEvent.titre}" modifié avec succès !`, 'success');
    
    setModal(null);
    setSelected(null);
    refresh(pagination.currentPage);
    
  } catch (error) {
    console.error("Erreur modification:", error);
    addNotification(
      `Erreur lors de la modification: ${error instanceof Error ? error.message : String(error)}`,
      'error'
    );
  } finally {
    setLoading(false);
  }
};

  const handleDelete = async (id: number) => {
  if (!window.confirm('Supprimer cet événement ?')) return;
  
  const token = localStorage.getItem('token');
  if (!token) {
    addNotification("Token d'authentification manquant !", 'error');
    return;
  }

  try {
    const response = await fetch(`http://localhost:8081/api/evenements/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` },
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Erreur lors de la suppression");
    }

    // Trouver l'événement supprimé pour afficher son titre dans la notification
    const deletedEvent = evenements.find(ev => ev.idEvenement === id);
    addNotification(
      `Événement "${deletedEvent?.titre || ''}" supprimé avec succès !`, 
      'success'
    );
    
    refresh(pagination.currentPage);
    
  } catch (error) {
    console.error("Erreur suppression:", error);
    addNotification(
      `Erreur lors de la suppression: ${error instanceof Error ? error.message : String(error)}`,
      'error'
    );
  }
};

  const handleLogout = () => {
    localStorage.removeItem('token');
  localStorage.removeItem('idUtilisateur');
  localStorage.removeItem('nomUtilisateur');
  localStorage.removeItem('prenomUtilisateur');
    router.push('/login');
  };
  // Fonction de conversion pour l'affichage dans le formulaire
// Fonction de conversion pour le formulaire
const formatForFormDisplay = (isoString: string) => {
  if (!isoString) return '';
  return isoString; // Ne plus compenser le timezone
};
// Formatage des dates en français
// Formatage des dates en français avec gestion des fuseaux horaires
const formatFrenchDateTime = (isoString: string) => {
  if (!isoString) return 'Non défini'
  
  try {
    const date = new Date(isoString)
    
    // Options de formatage
    const options: Intl.DateTimeFormatOptions = {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false // Format 24h
    }
    
    return new Intl.DateTimeFormat('fr-FR', options).format(date)
  } catch (error) {
    console.error('Erreur de formatage de date:', error)
    return isoString // Retourne la valeur originale en cas d'erreur
  }
}
const toInputFormat = (date: Date) => {
  // Ne surtout pas utiliser .toISOString() ici
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};


  // Helpers pour ouvrir les modales
  const openCreate = () => {
  const now = new Date();
  const oneHourLater = new Date(now.getTime() + 3600000);

  // Formatage initial en UTC+1
  const formatInitialDate = (date: Date) => {
    const localDate = new Date(date.getTime() + (60 * 60000)); // +1 heure
    return localDate.toISOString().slice(0, 19);
  };

  setForm({
    titre: '',
    description: '',
    capaciteMax: 0,
    lieu: '',
    dateDebut: formatInitialDate(now),
    dateFin: formatInitialDate(oneHourLater),
    type: { idType: 0 },
    statut: 'PROCHAIN'
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

  setForm({
    ...ev,
    type: ev.type || { idType: 0 },
    dateDebut: ev.dateDebut,
    dateFin: ev.dateFin,
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
        <div className={styles.branding}>
    <img src="/logo-maroc.png" alt="Logo" className={styles.logo} />
      <h1 className={styles.appTitle}>GE</h1>
  </div>
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
              <th>Type</th>
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
                <td>
        {types.find(t => t.idType === ev.type?.idType)?.typeEvent || 'N/A'}
      </td>
                <td>{formatDisplayDate(ev.dateDebut)}</td>
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
  <FrenchDateTimePicker
    selected={form.dateDebut}
    onChange={(date) => setForm(f => ({ ...f, dateDebut: date }))}
    label="Date de début "
  />
</div>

<div className={styles.formGroup}>
  <FrenchDateTimePicker
    selected={form.dateFin}
    onChange={(date) => setForm(f => ({ ...f, dateFin: date }))}
    label="Date de fin "
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
            <p><b>Date début :</b> {formatDisplayDate(selected.dateDebut)}</p>
            <p><b>Date fin :</b> {formatDisplayDate(selected.dateFin)}</p>
            <p><b>Lieu :</b> {selected.lieu}</p>
            <p><b>Statut :</b> {STATUTS.find(s => s.value === selected.statut)?.label || selected.statut}</p>
            <div className={styles.modalActions}>
              <button className={`${styles.modalBtn} ${styles.primary}`} onClick={() => setModal(null)}>Fermer</button>
            </div>
          </div>
        </div>
      )}
      {/* Toast notifications */}
<div className={styles.toastContainer}>
  {notifications.map(notification => (
    <div 
      key={notification.id} 
      className={`${styles.toast} ${styles[notification.type]}`}
    >
      {notification.message}
    </div>
  ))}
</div>
    </div>
  );
}