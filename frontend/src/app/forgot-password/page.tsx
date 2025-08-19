// app/forgot-password/page.tsx
'use client';

import React, { useState } from 'react';
import styles from '../login/Login.module.css';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const res = await fetch('http://localhost:8081/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });

      if (res.ok) {
        setMessage('Si cet email existe, vous recevrez un lien de réinitialisation');
      }
    } catch (err) {
      setMessage('Une erreur est survenue');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.loginWrapper}>
        <div className={styles.formWrapper}>
          <form onSubmit={handleSubmit} className={styles.form}>
            <h2>Mot de passe oublié</h2>
            <p>Entrez votre email pour recevoir un lien de réinitialisation</p>
            
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

            <button type="submit" className={styles.button} disabled={loading}>
              {loading ? 'Envoi en cours...' : 'Envoyer'}
            </button>

            {message && <div className={styles.message}>{message}</div>}
          </form>
        </div>
      </div>
    </div>
  );
}