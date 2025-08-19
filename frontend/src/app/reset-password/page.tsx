// app/reset-password/page.tsx
'use client';

import React, { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import styles from '../login/Login.module.css';

export default function ResetPassword() {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      setMessage('Les mots de passe ne correspondent pas');
      return;
    }

    setLoading(true);
    
    try {
      const res = await fetch('http://localhost:8081/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          token,
          newPassword: password
        }),
      });

      if (res.ok) {
        setMessage('Mot de passe réinitialisé avec succès');
        setTimeout(() => router.push('/login'), 2000);
      } else {
        setMessage('Erreur lors de la réinitialisation');
      }
    } catch (err) {
      setMessage('Erreur de connexion au serveur');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.loginWrapper}>
        <div className={styles.formWrapper}>
          <form onSubmit={handleSubmit} className={styles.form}>
            <h2>Réinitialiser le mot de passe</h2>
            
            <div>
              <label className={styles.label}>Nouveau mot de passe</label>
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
                className={styles.input}
              />
            </div>

            <div>
              <label className={styles.label}>Confirmer le mot de passe</label>
              <input
                type="password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                required
                className={styles.input}
              />
            </div>

            <button type="submit" className={styles.button} disabled={loading}>
              {loading ? 'Envoi en cours...' : 'Réinitialiser'}
            </button>

            {message && <div className={styles.message}>{message}</div>}
          </form>
        </div>
      </div>
    </div>
  );
}