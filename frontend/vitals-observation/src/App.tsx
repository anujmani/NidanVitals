import { useState } from 'react'
import './App.css'
import Dashboard from "./DashboardComponent/Dashboard";



function App() {
  const [count, setCount] = useState(0)

  return (
    <div >
      <h1>
        Nidan Vitals
      </h1>
      <Dashboard />
      
    </div>
  )
}

export default App
