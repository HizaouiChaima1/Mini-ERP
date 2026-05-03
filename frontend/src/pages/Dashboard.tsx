import { useEffect, useState } from 'react'
import { financeAPI, salesAPI, stockAPI } from '../api/client'
import { Loader, AlertCircle } from 'lucide-react'

export default function Dashboard() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [stats, setStats] = useState<any>(null)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setError(null)
        const [financeRes, ordersRes, productsRes] = await Promise.all([
          financeAPI.getDashboard().catch(() => ({ data: { totalMontant: 0 } })),
          salesAPI.getOrders().catch(() => ({ data: [] })),
          stockAPI.getProducts().catch(() => ({ data: [] })),
        ])

        setStats({
          totalRevenue: financeRes.data?.totalMontant || 0,
          totalOrders: ordersRes.data?.length || 0,
          totalProducts: productsRes.data?.length || 0,
          lastUpdate: new Date().toLocaleString('fr-FR'),
        })
      } catch (err: any) {
        console.error('Error fetching dashboard:', err)
        setError(err?.message || 'Erreur lors du chargement des données')
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader className="animate-spin w-8 h-8 text-blue-600" />
      </div>
    )
  }

  return (
    <div>
      <h2 className="text-3xl font-bold text-gray-900 mb-8">Tableau de Bord</h2>

      {error && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6 flex items-start">
          <AlertCircle className="w-5 h-5 text-yellow-600 mr-3 mt-0.5 flex-shrink-0" />
          <div>
            <p className="text-yellow-800">
              <strong>Note:</strong> {error}
            </p>
            <p className="text-sm text-yellow-700 mt-1">
              Assurez-vous que l'API Gateway s'exécute sur http://localhost:8080
            </p>
          </div>
        </div>
      )}

      {stats && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            {/* Revenue Card */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-gray-500 text-sm font-medium uppercase">Chiffre d'Affaires</h3>
              <p className="text-3xl font-bold text-gray-900 mt-2">
                {stats?.totalRevenue?.toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}
              </p>
              <p className="text-sm text-gray-500 mt-2">Depuis le démarrage</p>
            </div>

            {/* Orders Card */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-gray-500 text-sm font-medium uppercase">Commandes</h3>
              <p className="text-3xl font-bold text-gray-900 mt-2">{stats?.totalOrders}</p>
              <p className="text-sm text-gray-500 mt-2">Nombre total de commandes</p>
            </div>

            {/* Products Card */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-gray-500 text-sm font-medium uppercase">Produits</h3>
              <p className="text-3xl font-bold text-gray-900 mt-2">{stats?.totalProducts}</p>
              <p className="text-sm text-gray-500 mt-2">En catalogue</p>
            </div>
          </div>
        </>
      )}

   
    </div>
  )
}
