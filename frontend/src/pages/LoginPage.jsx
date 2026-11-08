import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/activities" replace />
  }

  async function onSubmit(event) {
    event.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email.trim(), password)
      navigate(location.state?.from || '/activities', { replace: true })
    } catch (err) {
      setError(err.message || 'Unable to sign in')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="hero-auth">
      <section className="hero-visual">
        <h1>Kinetic Fitness</h1>
        <p>Train with intention. Log every move. Let AI sharpen your next session.</p>
      </section>
      <section className="auth-panel">
        <div className="panel">
          <h2>Sign in</h2>
          <p className="lede">Welcome back. Continue where your last workout left off.</p>
          {error ? <div className="error-banner">{error}</div> : null}
          <form className="form-grid" onSubmit={onSubmit}>
            <label>
              Email
              <input
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </label>
            <label>
              Password
              <input
                type="password"
                autoComplete="current-password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </label>
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
          <p className="muted-link" style={{ marginTop: '1rem' }}>
            New here? <Link to="/signup">Create an account</Link>
          </p>
        </div>
      </section>
    </div>
  )
}
