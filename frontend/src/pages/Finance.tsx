import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { financeAPI } from '../api/client'
import { Loader, CreditCard, AlertCircle, TrendingUp } from 'lucide-react'

interface Invoice {
  id: number
  numero?: string
  commandeNumero?: string
  client: string
  montantHT?: number
  montantTTC?: number
  statut: string
  dateEcheance?: string
  datePaiement?: string
  createdAt?: string
  paiements?: Array<{
    montant: number
    mode: string
    reference: string
    date?: string
    createdAt?: string
  }>
}

interface Dashboard {
  chiffreAffairesHT: number
  chiffreAffairesTTC: number
  totalFactures: number
  facturesPayees: number
  facturesNonPayees: number
  facturesEnRetard: number
}

export default function Finance() {
  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [paymentForm, setPaymentForm] = useState<Invoice | null>(null)
  const [paymentData, setPaymentData] = useState({
    montant: '',
    mode: 'VIREMENT',
    reference: '',
  })
  const [paymentError, setPaymentError] = useState<string | null>(null)
  const [paymentSubmitting, setPaymentSubmitting] = useState(false)

  const fetchInvoices = useCallback(async () => {
    try {
      const res = await financeAPI.getInvoices()
      setInvoices(res.data || [])
    } catch (err: unknown) {
      console.error('Error fetching invoices:', err)
      setInvoices([])
    } finally {
      setLoading(false)
    }
  }, [])

  const fetchDashboard = useCallback(async () => {
    try {
      const res = await financeAPI.getDashboard()
      setDashboard(res.data || null)
    } catch (err: unknown) {
      console.error('Error fetching dashboard:', err)
      setDashboard(null)
    }
  }, [])

  const fetchData = useCallback(async () => {
    try {
      setError(null)
      await Promise.all([fetchInvoices(), fetchDashboard()])
    } catch (err: unknown) {
      console.error('Error fetching data:', err)
      setError('Impossible de charger les données')
    }
  }, [fetchInvoices, fetchDashboard])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  /** Montants API (BigDecimal) peuvent arriver comme nombre ou chaîne selon la sérialisation. */
  function parseInvoiceAmount(raw: unknown): number {
    if (typeof raw === 'number' && Number.isFinite(raw)) return raw
    if (typeof raw === 'string' && raw.trim() !== '') {
      const n = Number.parseFloat(raw)
      return Number.isFinite(n) ? n : Number.NaN
    }
    return Number.NaN
  }

  const handleRecordPayment = async (e: FormEvent) => {
    e.preventDefault()
    setPaymentError(null)
    try {
      if (!paymentForm) return

      const ttc = parseInvoiceAmount(paymentForm.montantTTC)
      const paidPartial =
        paymentForm.paiements?.reduce((sum, p) => {
          const cur = parseInvoiceAmount(p?.montant)
          return sum + (Number.isFinite(cur) ? cur : 0)
        }, 0) ?? 0
      const remaining = Number.isFinite(ttc) ? Math.max(0, ttc - (paymentForm.statut === 'PAYEE' ? ttc : paidPartial)) : Number.NaN

      const montant = Number.parseFloat(paymentData.montant.replace(',', '.'))
      if (!Number.isFinite(montant) || montant <= 0) {
        setPaymentError('Indiquez un montant valide (supérieur à 0).')
        return
      }
      if (Number.isFinite(remaining) && montant > remaining + 1e-6) {
        setPaymentError(`Le montant dépasse le solde à payer (${remaining.toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}).`)
        return
      }

      setPaymentSubmitting(true)
      await financeAPI.recordPayment(paymentForm.id, {
        montant,
        /** Doit correspondre à l'énumération serveur ModePaiement */
        mode: paymentData.mode,
        reference: paymentData.reference.trim() || undefined,
      })
      setPaymentForm(null)
      setPaymentData({ montant: '', mode: 'VIREMENT', reference: '' })
      await fetchData()
    } catch (error: unknown) {
      console.error('Error recording payment:', error)
      const axiosErr = error as { response?: { data?: { detail?: string }; status?: number } }
      const detail =
        typeof axiosErr.response?.data?.detail === 'string'
          ? axiosErr.response.data.detail
          : "Impossible d'enregistrer le paiement (vérifiez le montant et le mode)."
      setPaymentError(detail)
    } finally {
      setPaymentSubmitting(false)
    }
  }

  /** Ouvre le formulaire et réinitialise les messages du formulaire précédent. */
  function openPaymentForm(invoice: Invoice) {
    setPaymentError(null)
    setPaymentData({ montant: '', mode: 'VIREMENT', reference: '' })
    setPaymentForm(invoice)
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
      <h2 className="text-3xl font-bold text-gray-900 mb-6">Gestion Financière</h2>

      {error && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6 flex items-start">
          <AlertCircle className="w-5 h-5 text-yellow-600 mr-3 mt-0.5 flex-shrink-0" />
          <p className="text-yellow-800">{error}</p>
        </div>
      )}

      {/* Dashboard - Revenue Metrics */}
      {dashboard && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-lg shadow p-6 text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm opacity-90">CA HT</p>
                <p className="text-2xl font-bold">
                  {(dashboard.chiffreAffairesHT || 0).toLocaleString('fr-FR', {
                    style: 'currency',
                    currency: 'TND',
                    maximumFractionDigits: 0,
                  })}
                </p>
              </div>
              <TrendingUp className="w-12 h-12 opacity-30" />
            </div>
          </div>

          <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-lg shadow p-6 text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm opacity-90">CA TTC</p>
                <p className="text-2xl font-bold">
                  {(dashboard.chiffreAffairesTTC || 0).toLocaleString('fr-FR', {
                    style: 'currency',
                    currency: 'TND',
                    maximumFractionDigits: 0,
                  })}
                </p>
              </div>
              <TrendingUp className="w-12 h-12 opacity-30" />
            </div>
          </div>

          <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-lg shadow p-6 text-white">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm opacity-90">Payées</p>
                <p className="text-2xl font-bold">{dashboard.facturesPayees || 0}</p>
                <p className="text-xs opacity-75 mt-1">
                  sur {dashboard.totalFactures || 0} factures
                </p>
              </div>
              <CreditCard className="w-12 h-12 opacity-30" />
            </div>
          </div>

          <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-lg shadow p-6 text-white">
            <div>
              <p className="text-sm opacity-90 mb-3">Factures</p>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-xs">Impayées:</span>
                  <span className="font-semibold">{dashboard.facturesNonPayees || 0}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-xs">En retard:</span>
                  <span className="font-semibold">{dashboard.facturesEnRetard || 0}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {paymentForm && (
        <form
          onSubmit={handleRecordPayment}
          noValidate
          className="bg-white rounded-lg shadow p-6 mb-6 border border-gray-100"
        >
          <h3 className="font-bold text-lg mb-4">Enregistrer un paiement - Facture #{paymentForm.id}</h3>
          {paymentError && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
              {paymentError}
            </div>
          )}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">
                Montant (TND){' '}
                <span className="font-normal text-gray-500">
                  — max&nbsp;
                  {(parseInvoiceAmount(paymentForm.montantTTC) || 0).toLocaleString('fr-FR', {
                    style: 'currency',
                    currency: 'TND',
                  })}
                </span>
              </label>
              <input
                type="text"
                inputMode="decimal"
                placeholder="ex: 350.500"
                value={paymentData.montant}
                onChange={(e) => setPaymentData({ ...paymentData, montant: e.target.value })}
                className="w-full border rounded px-3 py-2"
                aria-invalid={!!paymentError}
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-gray-600">Mode</label>
              <select
                value={paymentData.mode}
                onChange={(e) => setPaymentData({ ...paymentData, mode: e.target.value })}
                className="w-full border rounded px-3 py-2"
              >
                <option value="VIREMENT">Virement</option>
                <option value="CHEQUE">Chèque</option>
                {/* Noms conformes au backend ModePaiement : ESPECES, CARTE */}
                <option value="ESPECES">Espèces</option>
                <option value="CARTE">Carte bancaire</option>
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="mb-1 block text-xs font-medium text-gray-600">
                Référence <span className="text-gray-400">(optionnel)</span>
              </label>
              <input
                type="text"
                placeholder="ex: VIR-2024-001"
                value={paymentData.reference}
                onChange={(e) => setPaymentData({ ...paymentData, reference: e.target.value })}
                className="w-full border rounded px-3 py-2"
              />
            </div>
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              disabled={paymentSubmitting}
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:opacity-60"
            >
              {paymentSubmitting ? 'Enregistrement…' : 'Enregistrer'}
            </button>
            <button
              type="button"
              disabled={paymentSubmitting}
              onClick={() => {
                setPaymentForm(null)
                setPaymentError(null)
              }}
              className="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500 disabled:opacity-60"
            >
              Annuler
            </button>
          </div>
        </form>
      )}

      <div className="space-y-4">
        {invoices.map((invoice) => {
          const ttc = Number(invoice?.montantTTC ?? NaN)
          if (invoice == null || typeof invoice.id !== 'number' || Number.isNaN(ttc)) {
            return null
          }

          const paidPartial =
            invoice.paiements?.reduce((sum, p) => sum + Number(p?.montant ?? 0), 0) ?? 0
          const paidAmount = invoice.statut === 'PAYEE' ? ttc : paidPartial
          const remaining = Math.max(0, ttc - paidAmount)
          const factureDateRaw = invoice.createdAt ?? invoice.dateEcheance

          return (
            <div key={invoice.id} className="bg-white rounded-lg shadow p-6">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div>
                  <p className="text-sm text-gray-600">Facture</p>
                  <p className="font-bold text-lg text-gray-900">#{invoice.id}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Client</p>
                  <p className="font-bold text-gray-900">{invoice.client || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-600">Date</p>
                  <p className="font-bold text-gray-900">
                    {factureDateRaw ? new Date(factureDateRaw).toLocaleDateString('fr-FR') : 'N/A'}
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4 py-4 border-y">
                <div className="bg-blue-50 rounded p-3">
                  <p className="text-sm text-gray-600">Montant Total</p>
                  <p className="font-bold text-lg text-blue-600">
                    {ttc.toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}
                  </p>
                </div>
                <div className="bg-green-50 rounded p-3">
                  <p className="text-sm text-gray-600">Payé</p>
                  <p className="font-bold text-lg text-green-600">
                    {(paidAmount || 0).toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}
                  </p>
                </div>
                <div className="bg-red-50 rounded p-3">
                  <p className="text-sm text-gray-600">À Payer</p>
                  <p className={`font-bold text-lg ${remaining > 0 ? 'text-red-600' : 'text-green-600'}`}>
                    {(remaining || 0).toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}
                  </p>
                </div>
              </div>

              <div className="mb-4">
                <span
                  className={`inline-block px-3 py-1 rounded text-sm font-semibold ${
                    invoice.statut === 'PAYEE'
                      ? 'bg-green-100 text-green-800'
                      : invoice.statut === 'EN_RETARD'
                        ? 'bg-orange-100 text-orange-800'
                        : invoice.statut === 'ANNULEE'
                          ? 'bg-gray-100 text-gray-800'
                          : 'bg-red-100 text-red-800'
                  }`}
                >
                  {invoice.statut === 'PAYEE'
                    ? 'Payée'
                    : invoice.statut === 'NON_PAYEE'
                      ? 'Non payée'
                      : invoice.statut === 'EN_RETARD'
                        ? 'En retard'
                        : invoice.statut === 'ANNULEE'
                          ? 'Annulée'
                          : invoice.statut}
                </span>
              </div>

              {invoice.paiements && invoice.paiements.length > 0 && (
                <div className="mb-4">
                  <p className="text-sm font-semibold text-gray-700 mb-2">Paiements:</p>
                  <div className="space-y-1 text-sm text-gray-600">
                    {invoice.paiements.map((payment, idx) => (
                      <p key={idx}>
                        • {payment?.mode || 'N/A'}: {(payment?.montant || 0).toLocaleString('fr-FR', { style: 'currency', currency: 'TND' })}{' '}
                        ({payment?.reference || 'N/A'}) -{' '}
                        {payment?.createdAt || payment?.date
                          ? new Date(payment.createdAt ?? payment.date ?? '').toLocaleDateString('fr-FR')
                          : 'N/A'}
                      </p>
                    ))}
                  </div>
                </div>
              )}

              {remaining > 0 && invoice.statut !== 'ANNULEE' && (
                <button
                  onClick={() => openPaymentForm(invoice)}
                  className="w-full bg-blue-600 text-white px-4 py-2 rounded flex items-center justify-center hover:bg-blue-700"
                >
                  <CreditCard className="w-4 h-4 mr-2" />
                  Enregistrer un Paiement
                </button>
              )}
            </div>
          )
        })}
      </div>

      {invoices.length === 0 && (
        <div className="text-center py-12 bg-white rounded-lg">
          <p className="text-gray-500">Aucune facture trouvée</p>
        </div>
      )}
    </div>
  )
}
