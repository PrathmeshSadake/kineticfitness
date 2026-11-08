import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function SignupPage() {
  const { signup, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/activities" replace />
  }

  function updateField(key, value) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await signup({
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
      })
      navigate('/activities', { replace: true })
    } catch (err) {
      setError(err.message || 'Unable to create account')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="hero-auth">
      <section className="hero-visual">
        <h1>Kinetic Fitness</h1>
        <p>Build a training log that remembers every set, mile, and recovery day.</p>
      </section>
      <section className="auth-panel">
        <div className="panel">
          <h2>Sign up</h2>
          <p className="lede">Create your account and start logging workouts in minutes.</p>
          {error ? <div className="error-banner">{error}</div> : null}
          <form className="form-grid" onSubmit={onSubmit}>
            <div className="form-grid two">
              <label>
                First name
                <input
                  required
                  value={form.firstName}
                  onChange={(e) => updateField('firstName', e.target.value)}
                />
              </label>
              <label>
                Last name
                <input
                  required
                  value={form.lastName}
                  onChange={(e) => updateField('lastName', e.target.value)}
                />
              </label>
            </div>
            <label>
              Email
              <input
                type="email"
                autoComplete="email"
                required
                value={form.email}
                onChange={(e) => updateField('email', e.target.value)}
              />
            </label>
            <label>
              Password
              <input
                type="password"
                autoComplete="new-password"
                required
                minLength={8}
                value={form.password}
                onChange={(e) => updateField('password', e.target.value)}
              />
            </label>
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Creating account…' : 'Create account'}
            </button>
          </form>
          <p className="muted-link" style={{ marginTop: '1rem' }}>
            Already training with us? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </section>
    </div>
  )
}
