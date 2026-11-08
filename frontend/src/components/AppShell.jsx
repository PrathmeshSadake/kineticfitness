import { Link, NavLink } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function AppShell({ children }) {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/activities" className="brand">
          <span className="brand-mark" aria-hidden="true" />
          Kinetic Fitness
        </Link>
        <nav className="nav-actions">
          <NavLink to="/activities" className="btn btn-ghost">
            Activities
          </NavLink>
          <NavLink to="/activities/new" className="btn btn-accent">
            Log activity
          </NavLink>
          <span className="meta">
            {user?.firstName} {user?.lastName}
          </span>
          <button type="button" className="btn btn-ghost" onClick={logout}>
            Log out
          </button>
        </nav>
      </header>
      <main className="page">{children}</main>
    </div>
  )
}
