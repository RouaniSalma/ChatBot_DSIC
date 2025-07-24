import React from 'react';
import Link from 'next/link';
import styles from './LandingPage.module.css';

export default function LandingPage() {
  return (
    <main className={styles.main}>
      {/* Header */}
      <header className={styles.header}>
        <div className={styles.container}>
          <h1 className={styles.logo}>EventWilaya</h1>
          <nav className={styles.nav}>
            <a href="#features">Fonctionnalités</a>
            <a href="#roles">Utilisateurs</a>
            <a href="#contact">Contact</a>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <section className={styles.hero}>
        <div className={styles.heroContent}>
          <h2>Simplifiez la gestion des événements dans votre wilaya</h2>
          <p>Une plateforme pour gérer conférences, forums, réunions, et plus encore, avec ou sans compte utilisateur.</p>
          <div className={styles.buttons}>
            <Link href="/participant"><button>Voir les événements</button></Link>
            <Link href="/login"><button className={styles.outline}>Connexion Admin</button></Link>
          </div>
        </div>
      </section>

      {/* Rôles */}
      <section className={styles.roles} id="roles">
        <h3>Pour chaque utilisateur, une mission claire</h3>
        <div className={styles.roleCards}>
          <div className={styles.card}>
            <h4>Participants</h4>
            <p>Inscrivez-vous aux événements sans compte, simplement et rapidement.</p>
          </div>
          <div className={styles.card}>
            <h4>Agents de Wilaya</h4>
            <p>Gérez les événements, les inscriptions et les informations des participants.</p>
          </div>
          <div className={styles.card}>
            <h4>Administrateurs</h4>
            <p>Surveillez, administrez les utilisateurs et gérez l’ensemble de la plateforme.</p>
          </div>
        </div>
      </section>

      {/* Appel à l'action */}
      <section className={styles.cta}>
        <h4>Commencez dès maintenant</h4>
        <p>Rejoignez la plateforme de gestion d’événements de votre wilaya.</p>
        <div className={styles.buttons}>
          <Link href="/participant"><button>Participer</button></Link>
          <Link href="/login"><button className={styles.outline}>Connexion</button></Link>
        </div>
      </section>

      {/* Footer */}
      <footer className={styles.footer} id="contact">
        <p>© 2025 EventWilaya — Tous droits réservés</p>
      </footer>
    </main>
  );
}
