import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { salesAPI, stockAPI } from '../api/client'
import { Loader, Plus, Check, Truck, X, AlertCircle } from 'lucide-react'

interface Order {
  id: number
  numero?: string
  client: string
  statut: string
  createdAt: string
  montantTotal: number
  lignes: Array<{
    produitReference: string
    produitNom?: string
    quantite: number
    prixUnitaire: number
    sousTotal?: number
  }>
}

interface ProductSummary {
  id: number
  reference: string
  nom: string
}

export default function Sales() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [products, setProducts] = useState<ProductSummary[]>([])
  const [formData, setFormData] = useState({
    client: '',
    lignes: [{ produitReference: '', quantite: '' }],
  })

  const fetchOrders = useCallback(async () => {
    try {
      const res = await salesAPI.getOrders()
      setOrders(res.data || [])
    } catch (error: unknown) {
      console.error('Error fetching orders:', error)
      setOrders([])
    } finally {
      setLoading(false)
    }
  }, [])

  const fetchProducts = useCallback(async () => {
    try {
      const res = await stockAPI.getProducts()
      setProducts(res.data || [])
    } catch (error: unknown) {
      console.error('Error fetching products:', error)
      setProducts([])
    }
  }, [])

  const fetchBoth = useCallback(async () => {
    try {
      setError(null)
      await Promise.all([fetchOrders(), fetchProducts()])
    } catch {
      setError('Erreur lors du chargement')
    }
  }, [fetchOrders, fetchProducts])

  useEffect(() => {
    fetchBoth()
  }, [fetchBoth])

  const handleCreateOrder = async (e: FormEvent) => {
    e.preventDefault()
    try {
      const orderData = {
        client: formData.client,
        lignes: formData.lignes.map((ligne) => ({
          produitReference: ligne.produitReference,
          quantite: parseInt(ligne.quantite),
        })),
      }
      await salesAPI.createOrder(orderData)
      setFormData({ client: '', lignes: [{ produitReference: '', quantite: '' }] })
      setShowForm(false)
      fetchOrders()
    } catch (error) {
      console.error('Error creating order:', error)
      alert('Erreur lors de la création de la commande')
    }
  }

  const handleConfirmOrder = async (orderId: number) => {
    try {
      await salesAPI.confirmOrder(orderId)
      fetchOrders()
    } catch (error) {
      console.error('Error confirming order:', error)
    }
  }

  const handleDeliverOrder = async (orderId: number) => {
    try {
      await salesAPI.deliverOrder(orderId)
      fetchOrders()
    } catch (error) {
      console.error('Error delivering order:', error)
    }
  }

  const handleCancelOrder = async (orderId: number) => {
    if (confirm('Confirmer l\'annulation?')) {
      try {
        await salesAPI.cancelOrder(orderId)
        fetchOrders()
      } catch (error) {
        console.error('Error canceling order:', error)
      }
    }
  }

  const handleDeleteOrder = async (orderId: number) => {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette commande ?')) {
      try {
        await salesAPI.deleteOrder(orderId)
        fetchOrders()
      } catch (error) {
        console.error('Error deleting order:', error)
        alert('Erreur lors de la suppression de la commande')
      }
    }
  }

  const addLineItem = () => {
    setFormData({
      ...formData,
      lignes: [...formData.lignes, { produitReference: '', quantite: '' }],
    })
  }

  const removeLineItem = (index: number) => {
    setFormData({
      ...formData,
      lignes: formData.lignes.filter((_, i) => i !== index),
    })
  }

  const updateLineItem = (index: number, field: string, value: string) => {
    const newLignes = [...formData.lignes]
    newLignes[index] = { ...newLignes[index], [field]: value }
    setFormData({ ...formData, lignes: newLignes })
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader className="animate-spin w-8 h-8 text-blue-600" />
      </div>
    )
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-3xl font-bold text-gray-900">Gestion des Ventes</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center hover:bg-blue-700"
        >
          <Plus className="w-4 h-4 mr-2" />
          Nouvelle Commande
        </button>
      </div>

      {error && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6 flex items-start">
          <AlertCircle className="w-5 h-5 text-yellow-600 mr-3 mt-0.5 flex-shrink-0" />
          <p className="text-yellow-800">{error}</p>
        </div>
      )}

      {showForm && (
        <form onSubmit={handleCreateOrder} className="bg-white rounded-lg shadow p-6 mb-6">
          <input
            type="text"
            placeholder="Client"
            value={formData.client}
            onChange={(e) => setFormData({ ...formData, client: e.target.value })}
            className="border rounded px-3 py-2 w-full mb-4"
            required
          />

          <div className="space-y-4 mb-4">
            <h3 className="font-semibold">Lignes de Commande</h3>
            {formData.lignes.map((ligne, index) => (
              <div key={index} className="flex gap-2">
                <select
                  value={ligne.produitReference}
                  onChange={(e) => updateLineItem(index, 'produitReference', e.target.value)}
                  className="flex-1 border rounded px-3 py-2"
                  required
                >
                  <option value="">Sélectionner un produit</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.reference}>
                      {p.nom} (Ref: {p.reference})
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  placeholder="Quantité"
                  value={ligne.quantite}
                  onChange={(e) => updateLineItem(index, 'quantite', e.target.value)}
                  className="border rounded px-3 py-2 w-24"
                  required
                />
                {formData.lignes.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeLineItem(index)}
                    className="bg-red-500 text-white px-3 py-2 rounded hover:bg-red-600"
                  >
                    Retirer
                  </button>
                )}
              </div>
            ))}
            <button
              type="button"
              onClick={addLineItem}
              className="bg-gray-500 text-white px-3 py-2 rounded hover:bg-gray-600 text-sm"
            >
              Ajouter une ligne
            </button>
          </div>

          <div className="flex gap-2">
            <button
              type="submit"
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Créer
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
            >
              Annuler
            </button>
          </div>
        </form>
      )}

      <div className="space-y-4">
        {orders.map((order) => (
          <div key={order.id} className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-start mb-4">
              <div>
                <h3 className="font-bold text-lg text-gray-900">Commande #{order.id}</h3>
                <p className="text-sm text-gray-600">Client: {order.client}</p>
                <p className="text-sm text-gray-600">
                  Date: {order.createdAt ? new Date(order.createdAt).toLocaleDateString('fr-FR') : '—'}
                </p>
              </div>
              <div className="text-right">
                <p className="text-2xl font-bold text-gray-900">
                  {(order.montantTotal ?? 0).toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}
                </p>
                <span className={`inline-block px-3 py-1 rounded text-sm font-semibold ${
                  order.statut === 'CONFIRMEE' ? 'bg-green-100 text-green-800' :
                  order.statut === 'LIVREE' ? 'bg-blue-100 text-blue-800' :
                  order.statut === 'ANNULEE' ? 'bg-red-100 text-red-800' :
                  'bg-yellow-100 text-yellow-800'
                }`}>
                  {order.statut}
                </span>
              </div>
            </div>

            <div className="mb-4 text-sm text-gray-600">
              <p className="font-semibold mb-2">Produits:</p>
              {order.lignes?.map((ligne, idx) => (
                <p key={idx}>
                  {ligne.produitNom ?? ligne.produitReference} × {ligne.quantite}
                </p>
              ))}
            </div>

            <div className="flex gap-2">
              {order.statut === 'EN_ATTENTE' && (
                <button
                  onClick={() => handleConfirmOrder(order.id)}
                  className="flex-1 bg-green-600 text-white px-4 py-2 rounded flex items-center justify-center hover:bg-green-700"
                >
                  <Check className="w-4 h-4 mr-2" />
                  Confirmer
                </button>
              )}
              {order.statut === 'CONFIRMEE' && (
                <button
                  onClick={() => handleDeliverOrder(order.id)}
                  className="flex-1 bg-blue-600 text-white px-4 py-2 rounded flex items-center justify-center hover:bg-blue-700"
                >
                  <Truck className="w-4 h-4 mr-2" />
                  Livrer
                </button>
              )}
              {order.statut !== 'ANNULEE' && order.statut !== 'LIVREE' && (
                <button
                  onClick={() => handleCancelOrder(order.id)}
                  className="flex-1 bg-red-600 text-white px-4 py-2 rounded flex items-center justify-center hover:bg-red-700"
                >
                  <X className="w-4 h-4 mr-2" />
                  Annuler
                </button>
              )}
              {(order.statut === 'ANNULEE' || order.statut === 'CONFIRMEE') && (
                <button
                  onClick={() => handleDeleteOrder(order.id)}
                  className="flex-1 bg-red-700 text-white px-4 py-2 rounded flex items-center justify-center hover:bg-red-800"
                >
                  <X className="w-4 h-4 mr-2" />
                  Supprimer
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      {orders.length === 0 && (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-gray-500">Aucune commande trouvée</p>
        </div>
      )}
    </div>
  )
}
