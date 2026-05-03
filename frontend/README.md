# 🎨 Mini-ERP Frontend

Interface utilisateur moderne pour gérer votre ERP — React + TypeScript + TailwindCSS

## 🚀 Démarrage rapide

### Mode développement

```bash
cd frontend
npm install
npm run dev
```

L'application s'ouvrira sur **http://localhost:3000**

### Mode production (avec Docker)

```bash
docker compose up frontend
```

Accessible sur **http://localhost:3000**

## 📋 Fonctionnalités

### 📊 Tableau de Bord

- Vue d'ensemble du chiffre d'affaires
- Nombre de commandes
- Nombre de produits en stock

### 📦 Gestion du Stock

- **Lister** tous les produits
- **Créer** un nouveau produit
- **Modifier** les quantités (entrée/sortie de stock)
- **Alertes** pour produits en dessous du seuil
- **Supprimer** un produit

### 🛒 Gestion des Ventes

- **Créer** une commande multilignes
- **Confirmer** une commande
- **Livrer** une commande
- **Annuler** une commande
- Suivi du statut en temps réel

### 💰 Gestion Financière

- **Lister** toutes les factures
- **Enregistrer** un paiement (virement, chèque, espèces, CB)
- Suivi des montants payés/restants
- Historique des paiements

## 🏗️ Architecture

```
frontend/
├── src/
│   ├── api/
│   │   └── client.ts           # Client Axios + endpoints API
│   ├── pages/
│   │   ├── Dashboard.tsx       # Tableau de bord
│   │   ├── Stock.tsx           # Gestion du stock
│   │   ├── Sales.tsx           # Gestion des ventes
│   │   └── Finance.tsx         # Gestion financière
│   ├── App.tsx                 # Router principal
│   ├── main.tsx                # Point d'entrée
│   └── index.css               # Styles globaux
├── package.json
├── vite.config.ts              # Config Vite
├── tailwind.config.js          # Config TailwindCSS
├── Dockerfile                  # Pour production
└── README.md
```

## 🔌 API Integration

Le frontend communique avec l'**API Gateway** sur `http://localhost:8080/api`

### Endpoints utilisés

**Stock:**

- `GET /api/stock/produits`
- `POST /api/stock/produits`
- `PUT /api/stock/produits/{id}`
- `DELETE /api/stock/produits/{id}`
- `POST /api/stock/produits/{id}/entree`
- `POST /api/stock/produits/{id}/sortie`

**Ventes:**

- `GET /api/ventes/commandes`
- `POST /api/ventes/commandes`
- `PATCH /api/ventes/commandes/{id}/confirmer`
- `PATCH /api/ventes/commandes/{id}/livrer`
- `PATCH /api/ventes/commandes/{id}/annuler`

**Finance:**

- `GET /api/finance/factures`
- `GET /api/finance/factures/dashboard`
- `POST /api/finance/factures/{id}/paiements`

## 🛠️ Stack Technique

- **React 18** — Interface utilisateur
- **TypeScript** — Typage statique
- **Vite** — Build tool ultra-rapide
- **TailwindCSS** — Styling
- **React Router v6** — Navigation
- **Axios** — HTTP client
- **Lucide React** — Icons

## 📦 Scripts

```bash
npm run dev       # Démarrage développement
npm run build     # Build pour production
npm run preview   # Prévisualiser le build
npm run lint      # Linter le code
```

## 🐳 Docker Compose

Le frontend est intégré au `docker-compose.yml`:

```bash
# Démarrer tout
docker compose up

# Démarrer seulement le frontend
docker compose up frontend

# Rebuild après changement de code
docker compose up --build frontend
```

## 🌐 Configuration

### Variables d'environnement

Créer un fichier `.env.local` (dev):

```
VITE_API_URL=http://localhost:8080/api
```

Ou via Docker (production):

```yaml
environment:
  REACT_APP_API_URL: http://api-gateway:8080/api
```

## ✨ Prochaines améliorations

- [ ] Authentification/Autorisation
- [ ] Graphiques et statistiques avancées
- [ ] Export PDF des factures
- [ ] Pagination/Filtres avancés
- [ ] Dark mode
- [ ] Notifications en temps réel (WebSocket)
- [ ] Tests e2e (Cypress)

## 📝 Notes

- L'API Gateway doit être en cours d'exécution sur le port 8080
- Les produits sont supprimés côté backend sans confirmation supplémentaire
- Les paiements partiels sont supportés

Bon développement! 🚀
