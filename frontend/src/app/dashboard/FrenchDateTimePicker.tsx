'use client'
import React from 'react'
import { format } from 'date-fns';
import DatePicker from 'react-datepicker'
import { registerLocale, setDefaultLocale } from 'react-datepicker'
import { fr } from 'date-fns/locale/fr'
import 'react-datepicker/dist/react-datepicker.css'

// Configuration de la locale française
registerLocale('fr', fr)
setDefaultLocale('fr')

interface FrenchDateTimePickerProps {
  selected: string
  onChange: (date: string) => void
  label: string
  minDate?: Date
}

export const FrenchDateTimePicker = ({
  selected,
  onChange,
  label,
  minDate
}: FrenchDateTimePickerProps) => {
  // Convertit la chaîne ISO en Date object pour le sélecteur
  const dateValue = selected ? new Date(selected) : null

  const handleChange = (date: Date | null) => {
  if (!date) {
    onChange('');
    return;
  }

  // Conversion en heure locale exacte (UTC+1 pour le Maroc)
  const localDate = new Date(date.getTime() + (60 * 60000)); // Ajoute 1 heure (60 minutes)
  
  // Formatage ISO sans timezone
  const isoString = localDate.toISOString().slice(0, 19); // "YYYY-MM-DDTHH:mm:ss"
  onChange(isoString);
};

  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium">{label}</label>
      <DatePicker
        selected={dateValue}
        onChange={handleChange}
        showTimeSelect
        timeFormat="HH:mm"
        timeIntervals={15}
        dateFormat="Pp" // Format français: "dd/MM/yyyy HH:mm"
        locale="fr"
        className="border rounded p-2 w-full"
        placeholderText="Sélectionnez une date et heure"
        timeCaption="Heure"
        minDate={minDate}
        isClearable
        shouldCloseOnSelect={false}
        timeInputLabel="Heure :"
      />
    </div>
  )
}