'use client'
import React from 'react'
import DatePicker from 'react-datepicker'
import { registerLocale, setDefaultLocale } from 'react-datepicker'
import { fr } from 'date-fns/locale/fr'
import 'react-datepicker/dist/react-datepicker.css'

registerLocale('fr', fr)
setDefaultLocale('fr')

interface FrenchDateTimePickerProps {
  selected: string | null
  onChange: (date: string) => void
  label: string
}

export const FrenchDateTimePicker = ({
  selected,
  onChange,
  label
}: FrenchDateTimePickerProps) => {
  const handleChange = (date: Date | null) => {
    if (!date) {
      onChange('')
      return
    }
    
    // Compensation du décalage horaire
    const adjustedDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
    onChange(adjustedDate.toISOString())
  }

  return (
    <div className="flex flex-col gap-1">
      <label className="text-sm font-medium">{label}</label>
      <DatePicker
        selected={selected ? new Date(selected) : null}
        onChange={handleChange}
        showTimeSelect
        timeFormat="HH:mm"
        timeIntervals={15}
        dateFormat="Pp"
        locale="fr"
        className="border rounded p-2 w-full"
        placeholderText="Sélectionnez une date"
        timeCaption="Heure"
      />
    </div>
  )
}