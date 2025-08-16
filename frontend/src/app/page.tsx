'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import SignaturePad from 'signature_pad';
import styles from './LandingPage.module.css';

interface Event {
  idEvenement: number;
  titre: string;
  description: string;
  lieu: string;
  dateDebut: string;
  dateFin: string;
  capaciteMax: number;
  statut: 'PROCHAIN' | 'EN_COURS' | 'TERMINE';
  imagePath?: string;
  type: {
    idType: number;
    typeEvent: string;
  };
}

interface ParticipantData {
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  signature: string;
  statutId: number | null;
}

interface StatusOption {
  idStatut: number;
  libelle: string;
}

const Home = () => {
  const [showConfirmation, setShowConfirmation] = useState(false);
  const carouselRef = React.useRef<HTMLDivElement>(null);
   const [isDragging, setIsDragging] = useState(false);
  const [startX, setStartX] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);
  const canvasRef = React.useRef<HTMLCanvasElement>(null);
  const signaturePadRef = React.useRef<SignaturePad | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [errors, setErrors] = useState<Record<string, string>>({});
  const [events, setEvents] = useState<Event[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [showParticipationForm, setShowParticipationForm] = useState(false);
  const [statusOptions, setStatusOptions] = useState<StatusOption[]>([]);
  const [participantData, setParticipantData] = useState<ParticipantData>({
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    signature: '',
    statutId: null
  });

  useEffect(() => {
    fetchEvents();
    fetchStatusOptions();
  }, []);

  useEffect(() => {
  if (!canvasRef.current) return;
  const canvas = canvasRef.current;
  canvas.width = canvas.offsetWidth;
  canvas.height = 200;
  signaturePadRef.current = new SignaturePad(canvas);
}, [showParticipationForm]);
const formatFrenchDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR');
};
// Gestion du défilement fluide
  const scrollToIndex = (index: number) => {
    if (!carouselRef.current) return;
    
    const item = carouselRef.current.children[index] as HTMLElement;
    if (!item) return;
    
    const container = carouselRef.current;
    const itemWidth = item.offsetWidth;
    const gap = 20; // doit correspondre à votre gap CSS
    const scrollPosition = index * (itemWidth + gap);
    
    container.scrollTo({
      left: scrollPosition,
      behavior: 'smooth'
    });
    
    setCurrentIndex(index);
  };


const validateField = (name: string, value: string) => {
    let error = '';
    
    switch (name) {
      case 'nom':
      case 'prenom':
        if (!value) error = 'Ce champ est obligatoire';
        else if (!/^[a-zA-ZÀ-ÿ\s\-']+$/.test(value)) error = 'Seules les lettres sont autorisées';
        break;
      case 'email':
        if (!value) error = 'Ce champ est obligatoire';
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) error = 'Email invalide';
        break;
      case 'telephone':
        if (!value) error = 'Ce champ est obligatoire';
        else if (!/^[0-9+]+$/.test(value)) error = 'Seuls les chiffres et le + sont autorisés';
        else if (value.length < 10) error = 'Le téléphone doit avoir au moins 10 chiffres';
        break;
      case 'statutId':
        if (!value) error = 'Veuillez sélectionner un statut';
        break;
    }
    
    return error;
  };
const checkEventCapacity = async (eventId: number) => {
  try {
    const response = await axios.get(`http://localhost:8081/api/participants/disponibilite/${eventId}`);
    return {
      disponible: response.data.disponible,
      capaciteMax: response.data.capaciteMax,
      participantsInscrits: response.data.participantsInscrits
    };
  } catch (error) {
    console.error("Erreur vérification capacité:", {
      error: error,
      eventId: eventId,
      url: `http://localhost:8081/api/participants/disponibilite/${eventId}`
    });
    return {
      disponible: false, // Changé à false pour bloquer en cas d'erreur
      capaciteMax: 0,
      participantsInscrits: 0
    };
  }
};

  const fetchEvents = async () => {
    try {
      const response = await axios.get('http://localhost:8081/api/public/evenements');
      setEvents(response.data);
    } catch (error) {
      console.error('Error fetching events:', error);
    }
  };

  const fetchStatusOptions = async () => {
    try {
      const response = await axios.get('http://localhost:8081/api/statuts-participant');
      setStatusOptions(response.data);
    } catch (error) {
      console.error('Error fetching status options:', error);
    }
  };

const nextSlide = () => {
    const newIndex = (currentIndex + 1) % events.length;
    scrollToIndex(newIndex);
  };

  const prevSlide = () => {
    const newIndex = (currentIndex - 1 + events.length) % events.length;
    scrollToIndex(newIndex);
  };
  // Gestion du glisser-déposer
  const handleMouseDown = (e: React.MouseEvent) => {
    if (!carouselRef.current) return;
    setIsDragging(true);
    setStartX(e.pageX - carouselRef.current.offsetLeft);
    setScrollLeft(carouselRef.current.scrollLeft);
  };
  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging || !carouselRef.current) return;
    e.preventDefault();
    const x = e.pageX - carouselRef.current.offsetLeft;
    const walk = (x - startX) * 2; // Multiplicateur pour un défilement plus rapide
    carouselRef.current.scrollLeft = scrollLeft - walk;
  };
  const openDetailsModal = async (event: Event) => {
  console.log('Ouverture modal pour événement ID:', event.idEvenement); // Ajout pour débogage
  setSelectedEvent(event);
  setShowDetailsModal(true);
  setShowParticipationForm(false);
  
  // Vérifier la capacité dès l'ouverture
  try {
    const { disponible, capaciteMax, participantsInscrits } = await checkEventCapacity(event.idEvenement);
    
    if (!disponible) {
      setErrorMessage(`La capacité maximale (${capaciteMax}) de cet événement est atteinte (${participantsInscrits} participants). Inscription fermée.`);
    } else {
      setErrorMessage(null);
    }
  } catch (error) {
    console.error("Erreur vérification capacité:", error);
    setErrorMessage("Erreur lors de la vérification des places disponibles");
  }
};

 const openParticipationForm = async () => {
  if (!selectedEvent) return;
  
  // Vérifier à nouveau la capacité au moment du clic
  try {
    const { disponible } = await checkEventCapacity(selectedEvent.idEvenement);
    
    if (!disponible) {
      setErrorMessage(`La capacité maximale est atteinte. Inscriptions closes.`);
      return;
    }
    
    setErrorMessage(null);
    setShowParticipationForm(true);
  } catch (error) {
    console.error("Erreur vérification capacité:", error);
    setErrorMessage("Erreur lors de la vérification des places disponibles");
  }
};
  const closeModal = () => {
    setShowDetailsModal(false);
    setShowParticipationForm(false);
    setSelectedEvent(null);
    setParticipantData({
      nom: '',
      prenom: '',
      email: '',
      telephone: '',
      signature: '',
      statutId: null
    });
  };

  const handleParticipantChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
  const { name, value } = e.target;

  const error = validateField(name, value);
  setErrors(prev => ({ ...prev, [name]: error }));

  // Conversion en nombre pour statutId
  const castedValue = name === 'statutId' 
    ? (value === '' ? null : Number(value))
    : value;

  setParticipantData(prev => ({
    ...prev,
    [name]: castedValue
  }));
};

const validateForm = () => {
  const newErrors: Record<string, string> = {};

  newErrors.nom = validateField('nom', participantData.nom);
  newErrors.prenom = validateField('prenom', participantData.prenom);
  newErrors.email = validateField('email', participantData.email);
  newErrors.telephone = validateField('telephone', participantData.telephone);
  newErrors.statutId = validateField('statutId', participantData.statutId?.toString() || '');

   // Vérification de la signature
if (!participantData.signature) {
    if (!signaturePadRef.current || signaturePadRef.current.isEmpty()) {
      newErrors.signature = 'La signature est obligatoire';
    } else {
      // Si le pad n'est pas vide mais participantData.signature n'est pas mis à jour
      const signatureData = signaturePadRef.current.toDataURL();
      setParticipantData(prev => ({ ...prev, signature: signatureData }));
    }
  }

  setErrors(newErrors);
  return Object.values(newErrors).every(error => !error);
};


const handleParticipate = async () => {
  if (!validateForm()) return;
  if (!selectedEvent) return;

  try {
    // Double vérification avant soumission
    const { disponible } = await checkEventCapacity(selectedEvent.idEvenement);
    if (!disponible) {
      setErrorMessage("La capacité a été atteinte pendant que vous remplissiez le formulaire. Inscription impossible.");
      return;
    }

    if (!participantData.signature && signaturePadRef.current && !signaturePadRef.current.isEmpty()) {
      const signatureData = signaturePadRef.current.toDataURL();
      setParticipantData(prev => ({ ...prev, signature: signatureData }));
    }
    
    setShowConfirmation(true);
    setErrorMessage(null);
    
  } catch (error) {
    console.error("Erreur vérification capacité:", error);
    setErrorMessage("Erreur lors de la vérification des places disponibles");
  }
};
const confirmParticipation = async () => {
  try {
    // Dernière vérification avant soumission
    // Dernière vérification avant soumission
    const { disponible } = await checkEventCapacity(selectedEvent?.idEvenement || 0);
    
     if (!disponible) {
      setErrorMessage("La capacité a été atteinte pendant que vous remplissiez le formulaire. Inscription impossible.");
      setShowConfirmation(false);
      return;
    }

    const payload = {
      nom: participantData.nom,
      prenom: participantData.prenom,
      email: participantData.email,
      telephone: participantData.telephone,
      signature: participantData.signature,
      statutId: Number(participantData.statutId)
    };

    const response = await axios.post(
      `http://localhost:8081/api/participants/inscription/${selectedEvent?.idEvenement}`,
      payload
    );
    
    alert('Inscription réussie !');
    closeModal();
  } catch (error: any) {
    if (error.response) {
      if (error.response.status === 400) {
        setErrorMessage(error.response.data?.error || "La capacité maximale est atteinte");
      } else {
        setErrorMessage(error.response.data?.error || "Erreur lors de l'inscription");
      }
    } else {
      setErrorMessage("Erreur de connexion au serveur");
    }
    setShowConfirmation(false);
  }
};
  const getStatusColor = (status: 'PROCHAIN' | 'EN_COURS' | 'TERMINE') => {
    switch (status) {
      case 'PROCHAIN':
        return styles.statusUpcoming;
      case 'EN_COURS':
        return styles.statusOngoing;
      case 'TERMINE':
        return styles.statusCompleted;
      default:
        return '';
    }
  };

  const getStatusLabel = (status: 'PROCHAIN' | 'EN_COURS' | 'TERMINE') => {
    switch (status) {
      case 'PROCHAIN':
        return 'À venir';
      case 'EN_COURS':
        return 'En cours';
      case 'TERMINE':
        return 'Terminé';
      default:
        return '';
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Événements - Préfecture Oujda Angad</h1>
      
      {events.length > 0 ? (
  <div className={styles.carousel}>
    <button 
      className={styles.carouselButton} 
      onClick={prevSlide}
      aria-label="Précédent"
    >
      &lt;
    </button>
    
    <div 
      className={styles.carouselContent} 
      ref={carouselRef}
      onMouseDown={handleMouseDown}
      onMouseLeave={handleMouseUp}
      onMouseUp={handleMouseUp}
      onMouseMove={handleMouseMove}
    >
      {events.map((event, index) => (
        <div 
          key={event.idEvenement} 
          className={`${styles.carouselItem} ${index === currentIndex ? styles.active : ''}`}
        >
          <div className={styles.eventCard} onClick={() => openDetailsModal(event)}>
            {event.imagePath ? (
              <img 
                src={`http://localhost:8081/api/images/${event.imagePath}`} 
                alt={event.titre} 
                className={styles.eventImage}
              />
            ) : (
              <div className={styles.noImage}>Pas d'image</div>
            )}
            <h3>{event.titre}</h3>
             <p>{formatFrenchDate(event.dateDebut)}</p>
            <div className={`${styles.statusBadge} ${getStatusColor(event.statut)}`}>
              {getStatusLabel(event.statut)}
            </div>
          </div>
        </div>
      ))}
    </div>
    
    <button 
      className={styles.carouselButton} 
      onClick={nextSlide}
      aria-label="Suivant"
    >
      &gt;
    </button>
  </div>
) : (
  <p className={styles.noEvents}>Aucun événement disponible pour le moment.</p>
)}
      
      {showDetailsModal && selectedEvent && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <button className={styles.closeButton} onClick={closeModal}>×</button>
            <h2>{selectedEvent.titre}</h2>
            
            <div className={styles.modalContent}>
              {selectedEvent.imagePath && (
                <img 
                  src={`http://localhost:8081/api/images/${selectedEvent.imagePath}`} 
                  alt={selectedEvent.titre} 
                  className={styles.modalImage}
                />
              )}
              
              <div className={styles.eventDetails}>
                <p><strong>Description:</strong> {selectedEvent.description}</p>
                <p><strong>Type:</strong> {selectedEvent.type.typeEvent}</p>
                <p><strong>Lieu:</strong> {selectedEvent.lieu}</p>
               <p><strong>Date de début:</strong> {formatFrenchDate(selectedEvent.dateDebut)}</p>
<p><strong>Date de fin:</strong> {formatFrenchDate(selectedEvent.dateFin)}</p>
                <p><strong>Capacité maximale:</strong> {selectedEvent.capaciteMax}</p>
                <div className={`${styles.statusBadge} ${getStatusColor(selectedEvent.statut)}`}>
                  {getStatusLabel(selectedEvent.statut)}
                </div>
              </div>
              
              {!showParticipationForm && selectedEvent.statut === 'PROCHAIN' && (
                <div className={styles.buttonSection}>
            {errorMessage && (
      <div className={styles.errorMessage}>
        {errorMessage}
      </div>
    )}
    <div className={styles.modalActions}>
      <button 
  className={styles.participateButton}
  onClick={openParticipationForm}
  disabled={!!errorMessage}
>
  {errorMessage ? 'Complet' : 'Participer'}
</button>
                  <button 
                    className={styles.closeModalButton}
                    onClick={closeModal}
                  >
                    Fermer
                  </button>
                </div>
                </div>
              )}
              
              {showParticipationForm && (
                <div className={styles.participationForm}>
                  <h3>Formulaire de participation</h3>
                  {errorMessage && (
      <div className={styles.errorMessage}>
        {errorMessage}
      </div>
    )}
                  <div className={styles.formGroup}>
        <label>Prénom:</label>
        <input 
          type="text" 
          name="prenom" 
          value={participantData.prenom} 
          onChange={handleParticipantChange} 
          required 
          className={errors.prenom ? styles.errorInput : ''}
        />
        {errors.prenom && <span className={styles.errorMessage}>{errors.prenom}</span>}
      </div>
      <div className={styles.formGroup}>
        <label>Nom:</label>
        <input 
          type="text" 
          name="nom" 
          value={participantData.nom} 
          onChange={handleParticipantChange} 
          required 
          className={errors.nom ? styles.errorInput : ''}
        />
        {errors.nom && <span className={styles.errorMessage}>{errors.nom}</span>}
      </div>
                  <div className={styles.formGroup}>
        <label>Email:</label>
        <input 
          type="email" 
          name="email" 
          value={participantData.email} 
          onChange={handleParticipantChange} 
          required 
          className={errors.email ? styles.errorInput : ''}
        />
        {errors.email && <span className={styles.errorMessage}>{errors.email}</span>}
      </div>
                  <div className={styles.formGroup}>
        <label>Téléphone:</label>
        <input 
          type="tel" 
          name="telephone" 
          value={participantData.telephone} 
          onChange={handleParticipantChange} 
          required 
          className={errors.telephone ? styles.errorInput : ''}
        />
        {errors.telephone && <span className={styles.errorMessage}>{errors.telephone}</span>}
      </div>
                 
                  <div className={styles.formGroup}>
  <label>Statut:</label>
  <select
  name="statutId"
  value={participantData.statutId || ''}
  onChange={handleParticipantChange}
>
  <option value="">Sélectionnez un statut</option>
  {statusOptions.map((status) => (
    <option key={status.idStatut} value={status.idStatut}>
      {status.libelle}
    </option>
  ))}
</select>
  {errors.statutId && <span className={styles.errorMessage}>{errors.statutId}</span>}
</div>
<div className={styles.formGroup}>
  <label>Signature:</label>
  <div className={styles.signaturePad}>
    <canvas 
      className={styles.signatureCanvas}
      ref={canvasRef}  // Ajoutez cette ref si nécessaire
    ></canvas>
    <button  className={styles.clearSignatureButton}
  onClick={() => {
    signaturePadRef.current?.clear();
    setParticipantData(prev => ({ ...prev, signature: "" }));
  }}
>
  Effacer
</button>
  </div>
  {errors.signature && (
    <span className={styles.errorMessage}>{errors.signature}</span>
  )}
</div>
                  <div className={styles.formActions}>
        <button 
  className={styles.submitButton} 
  onClick={handleParticipate}
  disabled={Object.values(errors).some(error => error)}
>
  Soumettre
</button>
        <button 
          className={styles.backButton}
          onClick={() => setShowParticipationForm(false)}
        >
          Retour
        </button>
      </div>
    </div>
              )}
            </div>
            {/* Ajoutez la modal de confirmation ici */}
{showConfirmation && (
  <>
    <div className={styles.confirmationOverlay} onClick={() => setShowConfirmation(false)} />
    <div className={styles.confirmationModal}>
      <button 
        className={styles.closeButton} 
        onClick={() => setShowConfirmation(false)}
        style={{ position: 'absolute', right: '15px', top: '15px' }}
      >
        ×
      </button>
      
      <h3>Confirmez vos informations</h3>
    <div className={styles.confirmationContent}>
      <p><strong>Nom:</strong> {participantData.nom}</p>
      <p><strong>Prénom:</strong> {participantData.prenom}</p>
      <p><strong>Email:</strong> {participantData.email}</p>
      <p><strong>Téléphone:</strong> {participantData.telephone}</p>
      <p><strong>Statut:</strong> {
        statusOptions.find(s => s.idStatut === participantData.statutId)?.libelle || 'Non spécifié'
      }</p>
      {participantData.signature && (
        <div className={styles.signaturePreview}>
          <p><strong>Signature:</strong></p>
          <img 
            src={participantData.signature} 
            alt="Signature preview" 
            className={styles.signatureImage}
          />
        </div>
      )}
    </div>
    <div className={styles.confirmationActions}>
      <button 
        className={styles.confirmButton}
        onClick={confirmParticipation}
      >
        Confirmer
      </button>
      <button 
        className={styles.editButton}
        onClick={() => setShowConfirmation(false)}
      >
        Modifier
      </button>
    </div>
  </div>
  </>
)}
          </div>
        </div>
      )}
      
      
      <footer className={styles.footer}>
        &copy; 2025 Ministère de l'Intérieur - Wilaya de la région de l'oriental - Préfecture d'Oujda Angad. Tous droits réservés.
      </footer>
    </div>
  );
};

export default Home;