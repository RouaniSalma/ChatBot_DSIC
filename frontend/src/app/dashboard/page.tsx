'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './Dashboard.module.css';

interface Evenement {
  idEvenement?: number;
  titre: string;
  dateDebut: string;
  statut: string; // "prochain", "en_cours", "termine"
}

const STATUTS = [
  { value: 'prochain', label: 'Prochain' },
  { value: 'en_cours', label: 'En cours' },
  { value: 'termine', label: 'Terminé' },
];

export default function Dashboard() {
  const [evenements, setEvenements] = useState<Evenement[]>([]);
  const [modal, setModal] = useState<'create' | 'edit' | 'details' | null>(null);
  const [selected, setSelected] = useState<Evenement | null>(null);
  const [form, setForm] = useState<Evenement>({ titre: '', dateDebut: '', statut: 'prochain' });
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  // Fetch events
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/login');
      return;
    }
    fetch('http://localhost:8080/api/evenements', {
      headers: { 'Authorization': `Bearer ${token}` },
    })
      .then(res => res.json())
      .then(data => setEvenements(data));
  }, [router]);

  // CRUD functions
  const refresh = async () => {
    const token = localStorage.getItem('token');
    const res = await fetch('http://localhost:8080/api/evenements', {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    setEvenements(await res.json());
  };

  const handleCreate = async () => {
    setLoading(true);
    const token = localStorage.getItem('token');
    await fetch('http://localhost:8080/api/evenements', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(form),
    });
    setModal(null);
    setForm({ titre: '', dateDebut: '', statut: 'prochain' });
    setLoading(false);
    refresh();
  };

  const handleEdit = async () => {
    if (!selected?.idEvenement) return;
    setLoading(true);
    const token = localStorage.getItem('token');
    await fetch(`http://localhost:8080/api/evenements/${selected.idEvenement}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
      body: JSON.stringify(form),
    });
    setModal(null);
    setSelected(null);
    setForm({ titre: '', dateDebut: '', statut: 'prochain' });
    setLoading(false);
    refresh();
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Supprimer cet événement ?')) return;
    const token = localStorage.getItem('token');
    await fetch(`http://localhost:8080/api/evenements/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` },
    });
    refresh();
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/login');
  };

  // Modal helpers
  const openCreate = () => {
    setForm({ titre: '', dateDebut: '', statut: 'prochain' });
    setModal('create');
  };
  const openEdit = (ev: Evenement) => {
    setSelected(ev);
    setForm(ev);
    setModal('edit');
  };
  const openDetails = (ev: Evenement) => {
    setSelected(ev);
    setModal('details');
  };

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
        <button className={styles.addBtn} onClick={openCreate}>+ Ajouter un événement</button>
      </div>

      {/* Tableau des événements */}
      <div className={styles.tableWrapper}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Titre</th>
              <th>Date de début</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {evenements.map(ev => (
              <tr key={ev.idEvenement}>
                <td>{ev.idEvenement}</td>
                <td>{ev.titre}</td>
                <td>{ev.dateDebut}</td>
                <td>
                  <span className={`${styles.status} ${styles[ev.statut]}`}>
                    {STATUTS.find(s => s.value === ev.statut)?.label}
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

      {/* Modals */}
      {(modal === 'create' || modal === 'edit') && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h2>{modal === 'create' ? 'Ajouter un événement' : 'Modifier un événement'}</h2>
            <label>Titre</label>
            <input
              value={form.titre}
              onChange={e => setForm(f => ({ ...f, titre: e.target.value }))}
              required
            />
            <label>Date de début</label>
            <input
              type="date"
              value={form.dateDebut}
              onChange={e => setForm(f => ({ ...f, dateDebut: e.target.value }))}
              required
            />
            <label>Statut</label>
            <select
              value={form.statut}
              onChange={e => setForm(f => ({ ...f, statut: e.target.value }))}
            >
              {STATUTS.map(s => (
                <option key={s.value} value={s.value}>{s.label}</option>
              ))}
            </select>
            <div className={styles.modalActions}>
              <button className={`${styles.modalBtn} ${styles.secondary}`} onClick={() => setModal(null)} type="button">Annuler</button>
              <button
                className={`${styles.modalBtn} ${styles.primary}`}
                onClick={modal === 'create' ? handleCreate : handleEdit}
                type="button"
                disabled={loading}
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
            <p><b>Date de début :</b> {selected.dateDebut}</p>
            <p><b>Statut :</b> {STATUTS.find(s => s.value === selected.statut)?.label}</p>
            <div className={styles.modalActions}>
              <button className={`${styles.modalBtn} ${styles.primary}`} onClick={() => setModal(null)}>Fermer</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}