import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import { Package, ShoppingCart, BarChart3, Home } from 'lucide-react'
import Dashboard from './pages/Dashboard'
import Stock from './pages/Stock'
import Sales from './pages/Sales'
import Finance from './pages/Finance'

function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <div className="min-h-screen bg-gray-50">
        {/* Navigation */}
        <nav className="bg-white shadow">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex items-center">
                <h1 className="text-2xl font-bold text-blue-600">Mini-ERP</h1>
              </div>
              <div className="flex space-x-1">
                <Link
                  to="/"
                  className="inline-flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
                >
                  <Home className="w-4 h-4 mr-2" />
                  Accueil
                </Link>
                <Link
                  to="/stock"
                  className="inline-flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
                >
                  <Package className="w-4 h-4 mr-2" />
                  Stock
                </Link>
                <Link
                  to="/sales"
                  className="inline-flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
                >
                  <ShoppingCart className="w-4 h-4 mr-2" />
                  Ventes
                </Link>
                <Link
                  to="/finance"
                  className="inline-flex items-center px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-50"
                >
                  <BarChart3 className="w-4 h-4 mr-2" />
                  Finance
                </Link>
              </div>
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/stock" element={<Stock />} />
            <Route path="/sales" element={<Sales />} />
            <Route path="/finance" element={<Finance />} />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  )
}

export default App
