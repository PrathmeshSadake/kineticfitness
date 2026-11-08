import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client'

function formatWhen(value) {
  if (!value) return '—'
  try {
    return new Date(value).toLocaleString()
  } catch {
    return String(value)
  }
}

export default function ActivityDetailPage() {
  const { activityId } = useParams()
  const [activity, setActivity] = useState(null)
  const [recommendation, setRecommendation] = useState(null)
  const [error, setError] = useState('')
  const [recMessage, setRecMessage] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError('')
      setRecMessage('')
      try {
        const activityData = await api.getActivity(activityId)
        if (cancelled) return
        setActivity(activityData)

        try {
          const rec = await api.getRecommendationByActivity(activityId)
          if (!cancelled) {
            setRecommendation(rec)
          }
        } catch (recErr) {
          if (!cancelled) {
            setRecommendation(null)
            setRecMessage(
              recErr.status === 404 || recErr.status === 502
                ? 'Recommendation is still generating. Refresh in a few seconds.'
                : recErr.message || 'Recommendation unavailable right now.',
            )
          }
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.message || 'Failed to load activity')
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
  }, [activityId])

  if (loading) {
    return <p className="meta">Loading activity…</p>
  }

  if (error) {
    return (
      <>
        <div className="error-banner">{error}</div>
        <Link to="/activities" className="btn btn-ghost">
          Back to activities
        </Link>
      </>
    )
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{(activity?.activityType || activity?.type)?.replaceAll('_', ' ')}</h1>
          <p>Session details and AI coaching for this workout.</p>
        </div>
        <Link to="/activities" className="btn btn-ghost">
          All activities
        </Link>
      </div>

      <div className="detail-grid">
        <section className="card-block">
          <h2>Session</h2>
          <div className="stat-row">
            <div className="stat">
              <strong>{activity?.duration ?? '—'}</strong>
              <span>Minutes</span>
            </div>
            <div className="stat">
              <strong>{activity?.caloriesBurned ?? '—'}</strong>
              <span>Calories</span>
            </div>
            <div className="stat">
              <strong>{(activity?.activityType || activity?.type)?.split('_')[0] || '—'}</strong>
              <span>Focus</span>
            </div>
          </div>
          <p className="meta">Started {formatWhen(activity?.startTime || activity?.createdAt)}</p>
          {activity?.additionalMetrics ? (
            <pre style={{ marginTop: '1rem', whiteSpace: 'pre-wrap', color: 'var(--muted)' }}>
              {JSON.stringify(activity.additionalMetrics, null, 2)}
            </pre>
          ) : null}
        </section>

        <section className="card-block">
          <h2>AI coaching</h2>
          {recMessage ? <p className="meta">{recMessage}</p> : null}
          {recommendation ? (
            <div className="rec-list">
              {(recommendation.recommendations || []).map((item, index) => (
                <div className="rec-item" key={`rec-${index}`}>
                  <h4>Recommendation</h4>
                  <p>{item}</p>
                </div>
              ))}
              {(recommendation.suggestions || []).map((item, index) => (
                <div className="rec-item" key={`sug-${index}`}>
                  <h4>Suggestion</h4>
                  <p>{item}</p>
                </div>
              ))}
              {(recommendation.safety || []).map((item, index) => (
                <div className="rec-item" key={`safe-${index}`}>
                  <h4>Safety</h4>
                  <p>{item}</p>
                </div>
              ))}
            </div>
          ) : null}
        </section>
      </div>
    </>
  )
}
