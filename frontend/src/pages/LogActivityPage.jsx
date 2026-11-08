import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client'

const ACTIVITY_TYPES = [
  'RUNNING',
  'WALKING',
  'CYCLING',
  'SWIMMING',
  'HIIT',
  'STRENGTH_TRAINING',
  'YOGA',
  'HIKING',
  'ROWING',
  'STRETCHING',
]

export default function LogActivityPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    type: 'RUNNING',
    duration: 30,
    caloriesBurned: 200,
    startTime: new Date().toISOString().slice(0, 16),
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function updateField(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      const created = await api.createActivity({
        type: form.type,
        duration: Number(form.duration),
        caloriesBurned: Number(form.caloriesBurned),
        startTime: new Date(form.startTime).toISOString(),
      })
      navigate(`/activities/${created.id}`, { replace: true })
    } catch (err) {
      setError(err.message || 'Failed to log activity')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Log activity</h1>
          <p>Capture the session details. AI coaching kicks in after save.</p>
        </div>
        <Link to="/activities" className="btn btn-ghost">
          Back
        </Link>
      </div>

      <div className="panel" style={{ width: 'min(560px, 100%)' }}>
        {error ? <div className="error-banner">{error}</div> : null}
        <form className="form-grid" onSubmit={onSubmit}>
          <label>
            Activity type
            <select value={form.type} onChange={(e) => updateField('type', e.target.value)}>
              {ACTIVITY_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type.replaceAll('_', ' ')}
                </option>
              ))}
            </select>
          </label>
          <div className="form-grid two">
            <label>
              Duration (minutes)
              <input
                type="number"
                min="1"
                required
                value={form.duration}
                onChange={(e) => updateField('duration', e.target.value)}
              />
            </label>
            <label>
              Calories burned
              <input
                type="number"
                min="0"
                required
                value={form.caloriesBurned}
                onChange={(e) => updateField('caloriesBurned', e.target.value)}
              />
            </label>
          </div>
          <label>
            Start time
            <input
              type="datetime-local"
              required
              value={form.startTime}
              onChange={(e) => updateField('startTime', e.target.value)}
            />
          </label>
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Saving…' : 'Save activity'}
          </button>
        </form>
      </div>
    </>
  )
}
