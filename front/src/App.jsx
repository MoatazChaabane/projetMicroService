import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import Layout from './components/layout/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Profile from './pages/Profile'
import EditProfile from './pages/EditProfile'
import ChangePassword from './pages/ChangePassword'
import Patients from './pages/Patients'
import Doctors from './pages/Doctors'
import DoctorDetail from './pages/DoctorDetail'
import Appointments from './pages/Appointments'
import MyAppointments from './pages/MyAppointments'
import FindDoctor from './pages/FindDoctor'
import Prescriptions from './pages/Prescriptions'
import PrescriptionDetail from './pages/PrescriptionDetail'
import MedicalRecord from './pages/MedicalRecord'
import './App.css'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter
        future={{
          v7_startTransition: true,
          v7_relativeSplatPath: true,
        }}
      >
        <div className="app">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/"
              element={
                <PrivateRoute>
                  <Layout />
                </PrivateRoute>
              }
            >
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
              <Route path="profile" element={<Profile />} />
              <Route path="profile/edit" element={<EditProfile />} />
              <Route path="profile/change-password" element={<ChangePassword />} />
              <Route path="patients" element={<Patients />} />
              <Route path="doctors" element={<Doctors />} />
              <Route path="doctors/:id" element={<DoctorDetail />} />
              <Route path="appointments" element={<Appointments />} />
              <Route path="my-appointments" element={<MyAppointments />} />
              <Route path="find-doctor" element={<FindDoctor />} />
              <Route path="prescriptions" element={<Prescriptions />} />
              <Route path="prescriptions/:id" element={<PrescriptionDetail />} />
              <Route path="medical-record" element={<MedicalRecord />} />
              <Route path="medical-record/:patientId" element={<MedicalRecord />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App

