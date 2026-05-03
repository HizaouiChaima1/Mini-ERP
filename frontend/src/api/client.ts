import axios, { AxiosInstance } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const apiClient: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
})

// Stock API
export const stockAPI = {
    getProducts: () => apiClient.get('/stock/produits'),
    getProduct: (id: number) => apiClient.get(`/stock/produits/${id}`),
    createProduct: (data: any) => apiClient.post('/stock/produits', data),
    updateProduct: (id: number, data: any) => apiClient.put(`/stock/produits/${id}`, data),
    deleteProduct: (id: number) => apiClient.delete(`/stock/produits/${id}`),
    addStock: (id: number, quantity: number) => apiClient.post(`/stock/produits/${id}/entree`, { quantite: quantity }),
    removeStock: (id: number, quantity: number) => apiClient.post(`/stock/produits/${id}/sortie`, { quantite: quantity }),
    getAlerts: () => apiClient.get('/stock/produits/alertes'),
}

// Sales API
export const salesAPI = {
    getOrders: () => apiClient.get('/ventes/commandes'),
    getOrder: (id: number) => apiClient.get(`/ventes/commandes/${id}`),
    createOrder: (data: any) => apiClient.post('/ventes/commandes', data),
    confirmOrder: (id: number) => apiClient.patch(`/ventes/commandes/${id}/confirmer`),
    deliverOrder: (id: number) => apiClient.patch(`/ventes/commandes/${id}/livrer`),
    cancelOrder: (id: number) => apiClient.patch(`/ventes/commandes/${id}/annuler`),
    deleteOrder: (id: number) => apiClient.delete(`/ventes/commandes/${id}`),
}

// Finance API
export const financeAPI = {
    getInvoices: () => apiClient.get('/finance/factures'),
    getInvoice: (id: number) => apiClient.get(`/finance/factures/${id}`),
    getDashboard: () => apiClient.get('/finance/factures/dashboard'),
    recordPayment: (id: number, data: any) => apiClient.post(`/finance/factures/${id}/paiements`, data),
    markDelayed: () => apiClient.post('/finance/factures/marquer-retard'),
}

export default apiClient
