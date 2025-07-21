'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './Login.module.css';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setError('');

  try {
    const res = await fetch('http://localhost:8081/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!res.ok) {
      setError('Email ou mot de passe incorrect');
      return;
    }

    // On suppose que le backend renvoie { token: "...", utilisateur: { idUtilisateur: ... } }
  const data = await res.json();
  console.log('Réponse backend:', data); // Ajoute ce log
  localStorage.setItem('token', data.token);
  localStorage.setItem('idUtilisateur', data.utilisateur.idUtilisateur);
  router.push('/dashboard');
  } catch (err) {
    setError('Erreur de connexion au serveur');
  }
};

  return (
    <div className={styles.container}>
      {/* Logo centered */}
      <div className={styles.header}>
  <img src="/logo-maroc.png" alt="Logo" className={styles.logo} />
</div>

      {/* Login form */}
      <div className={styles.formWrapper}>
        <form onSubmit={handleSubmit} className={styles.form}>
          <h2>Gestion des événements</h2>
          <div>
            <label className={styles.label}>Email</label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              className={styles.input}
            />
          </div>
          <div>
            <label className={styles.label}>Mot de passe</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              className={styles.input}
            />
          </div>
          {error && <div className={styles.error}>{error}</div>}
          <button type="submit" className={styles.button}>
            Se connecter
          </button>
        </form>
      </div>

      {/* Footer */}
      <footer className={styles.footer}>
        &copy; 2025 Ministère de l’Intérieur - Wilaya de la Région Oujda Angad. Tous droits réservés.
      </footer>
    </div>
  );
}