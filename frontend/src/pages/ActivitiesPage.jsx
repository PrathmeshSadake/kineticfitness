import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

function formatWhen(value) {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString()
  } catch {
    return String(value)
  }
}

export default function ActivitiesPage() {
  const [activities, setActivities] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await api.listActivities()
        if (!cancelled) {
          setActivities(Array.isArray(data) ? data : [])
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Failed to load activities')
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Your activities</h1>
          <p>Every logged session, ready for coaching insights.</p>
        </div>
        <Link to="/activities/new" className="btn btn-accent">
          Log activity
        </Link>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      {loading ? <p className="meta">Loading activities…</p> : null}

      {!loading && activities.length === 0 ? (
        <div className="empty">
          No activities yet. Log your first workout to unlock AI recommendations.
        </div>
      ) : null}

      <div className="activity-list">
        {activities.map((activity) => {
          const type = activity.activityType || activity.type
          return (
          <Link key={activity.id} to={`/activities/${activity.id}`} className="activity-row">
            <div>
              <span className="chip">{type}</span>
              <h3 style={{ marginTop: '0.45rem' }}>{type?.replaceAll('_', ' ')}</h3>
              <p className="meta">
                {activity.duration} min · {activity.caloriesBurned ?? 0} kcal ·{' '}
                {formatWhen(activity.startTime || activity.createdAt)}
              </p>
            </div>
            <span className="btn btn-ghost">View</span>
          </Link>
          )
        })}
      </div>
    </>
  )
}
