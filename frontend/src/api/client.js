const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

function getToken() {
  return localStorage.getItem('kf_token')
}

async function request(path, { method = 'GET', body, auth = true } = {}) {
  const headers = {
    Accept: 'application/json',
  }

  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  if (auth) {
    const token = getToken()
    if (!token) {
      const error = new Error('You are not signed in')
      error.status = 401
      throw error
    }
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : await response.text()

  if (!response.ok) {
    const message =
      (payload && typeof payload === 'object' && (payload.message || payload.error)) ||
      (typeof payload === 'string' && payload) ||
      `Request failed (${response.status})`
    const error = new Error(message)
    error.status = response.status
    error.payload = payload
    throw error
  }

  return payload
}

export const api = {
  signup: (data) => request('/api/v1/users/auth/signup', { method: 'POST', body: data, auth: false }),
  login: (data) => request('/api/v1/users/auth/login', { method: 'POST', body: data, auth: false }),
  me: () => request('/api/v1/users/me'),
  listActivities: () => request('/api/v1/activities'),
  getActivity: (id) => request(`/api/v1/activities/${id}`),
  createActivity: (data) => request('/api/v1/activities', { method: 'POST', body: data }),
  listRecommendations: (userId) => request(`/api/v1/ai/recommendations/user/${userId}`),
  getRecommendationByActivity: (activityId) =>
    request(`/api/v1/ai/recommendations/activity/${activityId}`),
}

export { API_BASE }
