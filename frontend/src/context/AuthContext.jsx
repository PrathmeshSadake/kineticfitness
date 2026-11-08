import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api } from '../api/client'

const AuthContext = createContext(null)

const TOKEN_KEY = 'kf_token'
const USER_KEY = 'kf_user'

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  })
  const [bootstrapping, setBootstrapping] = useState(Boolean(localStorage.getItem(TOKEN_KEY)))

  const persistSession = useCallback((authResponse) => {
    localStorage.setItem(TOKEN_KEY, authResponse.token)
    localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user))
    setToken(authResponse.token)
    setUser(authResponse.user)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const login = useCallback(async (email, password) => {
    const response = await api.login({ email, password })
    persistSession(response)
    return response
  }, [persistSession])

  const signup = useCallback(async (payload) => {
    const response = await api.signup(payload)
    persistSession(response)
    return response
  }, [persistSession])

  useEffect(() => {
    let cancelled = false
    async function hydrate() {
      if (!token) {
        setBootstrapping(false)
        return
      }
      try {
        const me = await api.me()
        if (!cancelled) {
          setUser(me)
          localStorage.setItem(USER_KEY, JSON.stringify(me))
        }
      } catch {
        if (!cancelled) {
          logout()
        }
      } finally {
        if (!cancelled) {
          setBootstrapping(false)
        }
      }
    }
    hydrate()
    return () => {
      cancelled = true
    }
  }, [token, logout])

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token && user),
      bootstrapping,
      login,
      signup,
      logout,
    }),
    [token, user, bootstrapping, login, signup, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
