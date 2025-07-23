'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import styles from './Login.module.css';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        setError('Email ou mot de passe incorrect');
        setLoading(false);
        return;
      }

      const data = await res.json();
      localStorage.setItem('token', data.token);
      localStorage.setItem('idUtilisateur', data.utilisateur.idUtilisateur);
      router.push('/dashboard');
    } catch (err) {
      setError('Erreur de connexion au serveur');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      

      {/* Layout en deux colonnes */}
      <div className={styles.loginWrapper}>
        {/* Illustration à gauche */}
        <div className={styles.imageSection}>
          <img src="/illustration.png" alt="Illustration" className={styles.illustration} />
        </div>

        {/* Formulaire à droite */}
        <div className={styles.formWrapper}>
          
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.logoContainer}>
    <img src="/logo-maroc.png" alt="Logo" className={styles.logo} />
    <div className={styles.divider}></div>
  </div>
            <h2>Gestion des événements </h2>

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
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
                className={styles.input}
              />
              <label className={styles.showPassword}>
                <input
                  type="checkbox"
                  checked={showPassword}
                  onChange={() => setShowPassword(!showPassword)}
                />
                Afficher le mot de passe
              </label>
            </div>

            {error && <div className={styles.error}>{error}</div>}

            <button type="submit" className={styles.button} disabled={loading}>
              {loading ? 'Connexion en cours...' : 'Se connecter'}
            </button>

            <div className={styles.forgot}>
              <a href="#">Mot de passe oublié ?</a>
            </div>
          </form>
        </div>
      </div>

      <footer className={styles.footer}>
        &copy; 2025 Ministère de l’Intérieur - Wilaya de la région de l'oriental - Préfecture d'Oujda Angad. Tous droits réservés.
      </footer>
    </div>
  );
}
