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

      const token = await res.text();
      localStorage.setItem('token', token);
      router.push('/dashboard');
    } catch (err) {
      setError('Erreur de connexion au serveur');
    }
  };

  return (
    <div className={styles.container}>
      {/* Header with logo and title */}
      <div className={styles.header}>
        <img src="/logo.png" alt="Logo" className={styles.logo} />
        <div className={styles.headerText}>
          <h1>MINISTRE DE L’INTERIEUR</h1>
          <h2>WILAYA DE LA REGION OUJDA ANGAD</h2>
        </div>
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