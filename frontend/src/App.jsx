import { Navigate, Route, Routes } from 'react-router-dom'
import AppShell from './components/AppShell'
import ProtectedRoute from './components/ProtectedRoute'
import ActivitiesPage from './pages/ActivitiesPage'
import ActivityDetailPage from './pages/ActivityDetailPage'
import LogActivityPage from './pages/LogActivityPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/activities" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route
        path="/activities"
        element={
          <ProtectedRoute>
            <AppShell>
              <ActivitiesPage />
            </AppShell>
          </ProtectedRoute>
        }
      />
      <Route
        path="/activities/new"
        element={
          <ProtectedRoute>
            <AppShell>
              <LogActivityPage />
            </AppShell>
          </ProtectedRoute>
        }
      />
      <Route
        path="/activities/:activityId"
        element={
          <ProtectedRoute>
            <AppShell>
              <ActivityDetailPage />
            </AppShell>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/activities" replace />} />
    </Routes>
  )
}
