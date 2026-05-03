# ✅ Frontend Mini-ERP — Installation Terminée

Votre interface de gestion ERP est prête! 🎉

## 📁 Fichiers créés

```
frontend/
├── src/
│   ├── api/
│   │   └── client.ts              # Client API (axios)
│   ├── pages/
│   │   ├── Dashboard.tsx          # Tableau de bord
│   │   ├── Stock.tsx              # Gestion produits
│   │   ├── Sales.tsx              # Gestion commandes
│   │   └── Finance.tsx            # Gestion factures
│   ├── hooks/
│   │   └── useAsync.ts            # Hook utilitaire
│   ├── App.tsx                    # Routeur principal
│   ├── main.tsx                   # Point d'entrée
│   └── index.css                  # Styles TailwindCSS
├── index.html                     # Template HTML
├── package.json                   # Dépendances
├── vite.config.ts                 # Config Vite
├── tailwind.config.js             # Config Tailwind
├── tsconfig.json                  # Config TypeScript
├── Dockerfile                     # Pour production
├── .env.example                   # Variables d'env
├── README.md                      # Documentation
└── FRONTEND_SETUP.md              # Ce fichier
```

## 🚀 Démarrage

### Option 1: Development local (recommandé)

```bash
cd frontend
npm install
npm run dev
```

L'app s'ouvrira sur **http://localhost:3000**

### Option 2: Avec Docker Compose

```bash
# Terminal principal
docker compose up

# Dans un autre terminal
docker compose up --build frontend
```

Accédez sur **http://localhost:3000**

## 🎯 Fonctionnalités implémentées

### 1️⃣ Tableau de Bord (`/`)

- Vue d'ensemble des KPIs
- Chiffre d'affaires total
- Nombre de commandes
- Nombre de produits

### 2️⃣ Stock (`/stock`)

✅ Créer un produit
✅ Lister tous les produits (en cartes)
✅ Ajouter du stock (entrée)
✅ Retirer du stock (sortie)
✅ Alertes visuelles (seuil)
✅ Supprimer un produit

### 3️⃣ Ventes (`/sales`)

✅ Créer une commande (multilignes)
✅ Lister les commandes
✅ Confirmer une commande
✅ Livrer une commande
✅ Annuler une commande
✅ Suivi du statut
✅ Affichage du total

### 4️⃣ Finance (`/finance`)

✅ Lister les factures
✅ Enregistrer un paiement
✅ Suivi montant payé / restant
✅ Historique des paiements
✅ Choix du mode de paiement
✅ Dashboard financier

## 🔌 Intégration API

**URL Base:** `http://localhost:8080/api`

L'app se connecte automatiquement à l'API Gateway. Tous les appels sont centralisés dans `src/api/client.ts`.

### Endpoints utilisés

**Stock:**

```
GET    /stock/produits
POST   /stock/produits
PUT    /stock/produits/{id}
DELETE /stock/produits/{id}
POST   /stock/produits/{id}/entree
POST   /stock/produits/{id}/sortie
```

**Ventes:**

```
GET    /ventes/commandes
POST   /ventes/commandes
PATCH  /ventes/commandes/{id}/confirmer
PATCH  /ventes/commandes/{id}/livrer
PATCH  /ventes/commandes/{id}/annuler
```

**Finance:**

```
GET    /finance/factures
GET    /finance/factures/dashboard
POST   /finance/factures/{id}/paiements
```

## 🛠️ Stack technique utilisée

| Composant    | Version | Rôle         |
| ------------ | ------- | ------------ |
| React        | 18.2    | Framework UI |
| TypeScript   | 5.3     | Typage       |
| Vite         | 5.0     | Build tool   |
| TailwindCSS  | 3.4     | Styles       |
| Axios        | 1.6     | HTTP client  |
| React Router | 6.20    | Navigation   |
| Lucide Icons | 0.294   | Icônes       |

## 📋 Checklist pour utiliser

- [x] Frontend créé avec React + TypeScript
- [x] Pages pour Stock, Ventes, Finance, Dashboard
- [x] Client API intégré
- [x] UI responsive (mobile + desktop)
- [x] Docker ready
- [x] Intégration docker-compose.yml
- [x] Documentation complète

## 🎨 Interface

### Thème

- **Couleurs:** Blue, Green, Red, Gray, Yellow, Orange
- **Responsive:** Mobile-first avec TailwindCSS
- **Icons:** Lucide React
- **Composants:** Cartes, formulaires, tableaux, boutons

### Navigation

Barre de navigation fixe avec liens vers:

- Accueil (Dashboard)
- Stock
- Ventes
- Finance

## 🚨 Prérequis pour l'exécution

1. ✅ **Tous les services backend doivent être en cours d'exécution**
   - API Gateway sur 8080
   - Stock Service sur 8081
   - Ventes Service sur 8082
   - Finance Service sur 8083

2. ✅ **Bases de données doivent être initialisées**

3. ✅ **RabbitMQ doit être actif**

## 📝 Notes importantes

- L'app utilise des `alert()` JavaScript (à remplacer par des toasts pour UX meilleure)
- Les erreurs API s'affichent en console (amélioration: toast notifications)
- Le frontend proxifie les appels vers le backend en développement
- En production, le frontend se compile en HTML/CSS/JS statiques

## 🔄 Mise à jour du code

Modifiez les fichiers dans `frontend/src/`:

```bash
# En dev
npm run dev          # Hot reload automatique

# En production
npm run build        # Build
npm run preview      # Tester le build
```

## 🐛 Dépannage

**Port 3000 déjà utilisé:**

```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :3000
kill -9 <PID>
```

**API introuvable:**

- Vérifier que le gateway est sur `http://localhost:8080`
- Vérifier les logs: `docker compose logs api-gateway`

**Erreur "CORS":**

- Vérifier que le gateway autorise les origins CORS
- Vérifier l'URL de l'API

## 🚀 Prochaines étapes (optionnel)

1. Ajouter des tests unitaires (Vitest)
2. Ajouter des tests e2e (Cypress)
3. Authentification (JWT)
4. Dark mode
5. Graphiques (Chart.js / Recharts)
6. Export PDF
7. Notifications temps réel (WebSocket)
8. Pagination/Filtres avancés
9. Historique des actions (audit)
10. Paramètres utilisateur

---

**C'est tout! Bon développement! 🎉**

Pour plus d'infos, voir `frontend/README.md`
