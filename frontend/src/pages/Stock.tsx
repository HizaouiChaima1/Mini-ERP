import { useEffect, useState, type FormEvent } from 'react'
import { stockAPI } from '../api/client'
import { Loader, Plus, Trash2, ArrowUp, ArrowDown, AlertCircle } from 'lucide-react'

interface Product {
  id: number
  reference: string
  nom: string
  prixUnitaire: number
  quantiteEnStock: number
  seuilAlerte: number
}

export default function Stock() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({
    reference: '',
    nom: '',
    prixUnitaire: '',
    quantiteEnStock: '',
    seuilAlerte: '',
  })

  useEffect(() => {
    fetchProducts()
  }, [])

  const fetchProducts = async () => {
    try {
      setError(null)
      const res = await stockAPI.getProducts()
      setProducts(res.data || [])
    } catch (err: unknown) {
      console.error('Error fetching products:', err)
      setError('Impossible de charger les produits')
      setProducts([])
    } finally {
      setLoading(false)
    }
  }

  const handleCreateProduct = async (e: FormEvent) => {
    e.preventDefault()
    try {
      await stockAPI.createProduct({
        reference: formData.reference,
        nom: formData.nom,
        prixUnitaire: parseFloat(formData.prixUnitaire),
        quantiteEnStock: parseInt(formData.quantiteEnStock),
        seuilAlerte: parseInt(formData.seuilAlerte),
      })
      setFormData({ reference: '', nom: '', prixUnitaire: '', quantiteEnStock: '', seuilAlerte: '' })
      setShowForm(false)
      fetchProducts()
    } catch (error: unknown) {
      console.error('Error creating product:', error)
      let msg = 'Impossible de créer le produit'
      if (error && typeof error === 'object' && 'response' in error) {
        const data = (error as { response?: { data?: { message?: unknown } } }).response?.data
        if (data?.message !== undefined && data.message !== null) msg = String(data.message)
      } else if (error instanceof Error) msg = error.message
      alert('Erreur: ' + msg)
    }
  }

  const handleAddStock = async (productId: number) => {
    const quantity = prompt('Quantité à ajouter:')
    if (quantity) {
      try {
        await stockAPI.addStock(productId, parseInt(quantity))
        fetchProducts()
      } catch (error: unknown) {
        console.error('Error adding stock:', error)
        alert('Erreur lors de l\'ajout de stock')
      }
    }
  }

  const handleRemoveStock = async (productId: number) => {
    const quantity = prompt('Quantité à retirer:')
    if (quantity) {
      try {
        await stockAPI.removeStock(productId, parseInt(quantity))
        fetchProducts()
      } catch (error: unknown) {
        console.error('Error removing stock:', error)
        alert('Erreur lors du retrait de stock')
      }
    }
  }

  const handleDeleteProduct = async (productId: number) => {
    if (confirm('Confirmer la suppression?')) {
      try {
        await stockAPI.deleteProduct(productId)
        fetchProducts()
      } catch (error: unknown) {
        console.error('Error deleting product:', error)
        alert('Erreur lors de la suppression')
      }
    }
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
        <h2 className="text-3xl font-bold text-gray-900">Gestion du Stock</h2>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg flex items-center hover:bg-blue-700"
        >
          <Plus className="w-4 h-4 mr-2" />
          Nouveau Produit
        </button>
      </div>

      {error && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6 flex items-start">
          <AlertCircle className="w-5 h-5 text-yellow-600 mr-3 mt-0.5 flex-shrink-0" />
          <p className="text-yellow-800">{error}</p>
        </div>
      )}

      {showForm && (
        <form onSubmit={handleCreateProduct} className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input
              type="text"
              placeholder="Référence"
              value={formData.reference}
              onChange={(e) => setFormData({ ...formData, reference: e.target.value })}
              className="border rounded px-3 py-2"
              required
            />
            <input
              type="text"
              placeholder="Nom"
              value={formData.nom}
              onChange={(e) => setFormData({ ...formData, nom: e.target.value })}
              className="border rounded px-3 py-2"
              required
            />
            <input
              type="number"
              placeholder="Prix Unitaire"
              value={formData.prixUnitaire}
              onChange={(e) => setFormData({ ...formData, prixUnitaire: e.target.value })}
              className="border rounded px-3 py-2"
              step="0.01"
              required
            />
            <input
              type="number"
              placeholder="Quantité"
              value={formData.quantiteEnStock}
              onChange={(e) => setFormData({ ...formData, quantiteEnStock: e.target.value })}
              className="border rounded px-3 py-2"
              required
            />
            <input
              type="number"
              placeholder="Seuil d'Alerte"
              value={formData.seuilAlerte}
              onChange={(e) => setFormData({ ...formData, seuilAlerte: e.target.value })}
              className="border rounded px-3 py-2"
              required
            />
          </div>
          <div className="flex gap-2 mt-4">
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

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product) => (
          <div key={product.id} className="bg-white rounded-lg shadow p-6">
            <div className="mb-4">
              <h3 className="font-bold text-lg text-gray-900">{product.nom}</h3>
              <p className="text-sm text-gray-600">Ref: {product.reference}</p>
            </div>

            <div className="mb-4 space-y-2">
              <p className="text-sm text-gray-700">
                <span className="font-semibold">Prix:</span> {product.prixUnitaire.toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}
              </p>
              <div className="flex items-center justify-between">
                <span className="text-sm text-gray-700">
                  <span className="font-semibold">Stock:</span> {product.quantiteEnStock}
                </span>
                {product.quantiteEnStock <= product.seuilAlerte && (
                  <span className="text-xs bg-red-100 text-red-800 px-2 py-1 rounded">Alerte</span>
                )}
              </div>
              <p className="text-sm text-gray-700">
                <span className="font-semibold">Seuil:</span> {product.seuilAlerte}
              </p>
            </div>

            <div className="flex gap-2 justify-between">
              <button
                onClick={() => handleAddStock(product.id)}
                className="flex-1 bg-green-500 text-white px-3 py-2 rounded flex items-center justify-center hover:bg-green-600 text-sm"
              >
                <ArrowUp className="w-4 h-4 mr-1" />
                Entrée
              </button>
              <button
                onClick={() => handleRemoveStock(product.id)}
                className="flex-1 bg-orange-500 text-white px-3 py-2 rounded flex items-center justify-center hover:bg-orange-600 text-sm"
              >
                <ArrowDown className="w-4 h-4 mr-1" />
                Sortie
              </button>
              <button
                onClick={() => handleDeleteProduct(product.id)}
                className="flex-1 bg-red-500 text-white px-3 py-2 rounded flex items-center justify-center hover:bg-red-600"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {products.length === 0 && (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-gray-500">Aucun produit trouvé</p>
        </div>
      )}
    </div>
  )
}
