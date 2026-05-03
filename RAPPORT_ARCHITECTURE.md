# 📋 RAPPORT D'ARCHITECTURE - Mini-ERP

**Date** : Mai 2026  
**Version** : 1.0.0  
**Projet** : Mini-ERP — Système de Gestion d'Entreprise Microservices

---

## 📑 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture générale](#architecture-générale)
3. [Services du projet](#services-du-projet)
4. [Communication inter-services](#communication-inter-services)
5. [Base de données](#base-de-données)
6. [API endpoints](#api-endpoints)
7. [Flux de données](#flux-de-données)
8. [Technologies utilisées](#technologies-utilisées)

---

## 🎯 Vue d'ensemble

Le projet **Mini-ERP** est une application de gestion d'entreprise basée sur une architecture **microservices** avec les technologies modernes :

- **Backend** : 4 services Java/Spring Boot 3.2 + 1 API Gateway
- **Frontend** : Interface React 18 avec TypeScript et Vite
- **Bases de données** : PostgreSQL (une par service)
- **Communication** : REST (synchrone) + RabbitMQ (asynchrone)
- **Orchestration** : Docker Compose

**Objectif** : Fournir une plateforme intégrée pour gérer :

- 📦 **Stock** : Inventaire des produits
- 🛒 **Ventes** : Gestion des commandes clients
- 💰 **Finance** : Gestion des factures et paiements

---

## 🏗️ Architecture générale

### Diagramme de l'architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React)                         │
│              http://localhost:3000 (Port Vite)                  │
│  Client TypeScript/React + Axios pour les appels API           │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         │ HTTP REST
                         │
┌────────────────────────▼─────────────────────────────────────────┐
│                   API GATEWAY (Spring Cloud)                    │
│                  http://localhost:8080                          │
│                                                                 │
│  Route /api/stock/** → Stock Service                          │
│  Route /api/ventes/** → Ventes Service                        │
│  Route /api/finance/** → Finance Service                      │
│                                                                 │
│  ✓ CORS configuré pour localhost:3000, 3001                   │
│  ✓ StripPrefix: supprime /api/<service>                       │
└─┬──────────────────┬──────────────────┬────────────────────────┘
  │                  │                  │
  │                  │                  │
  ▼                  ▼                  ▼
┌─────────────┐  ┌──────────────┐  ┌──────────────┐
│   STOCK     │  │   VENTES     │  │   FINANCE    │
│  :8081      │  │   :8082      │  │   :8083      │
└──┬──────────┘  └──┬───────────┘  └──┬───────────┘
   │                │                  │
   │ REST Feign    │ REST Feign      │
   │ (Sync)        │ (Sync)          │
   │ RMI (Sync)    │ RMI (Sync)      │  ← Java RMI Synchrone
   │ :1099-1101    │ :1100-1101      │     Fortement typé
   │                │                  │
   └────────────────┼──────────────────┤
                    │                  │
                    │                  │
                    ▼                  ▼
         ┌──────────────────────────────────┐
         │   RabbitMQ Message Broker        │
         │   localhost:5672                 │
         │   Management: localhost:15672    │
         │                                  │
         │  Exchange: erp.exchange          │
         │  Topic: commande.created         │
         │  Queue: finance.commande.queue   │
         └──────────────────────────────────┘
           │                  │                  │
           ▼                  ▼                  ▼
       ┌────────┐         ┌────────┐         ┌────────┐
       │ Stock  │         │ Ventes │         │Finance │
       │  DB    │         │  DB    │         │  DB    │
       │ :5433  │         │ :5434  │         │ :5435  │
       └────────┘         └────────┘         └────────┘
     PostgreSQL        PostgreSQL          PostgreSQL
     stock_db          ventes_db           finance_db
```

---

## � Communication inter-services : RMI

### Java RMI (Remote Method Invocation)

Java RMI a été intégré pour permettre une **communication synchrone et fortement typée** entre les services microservices.

#### Ports RMI

| Service         | Port | URL RMI                                     |
| --------------- | ---- | ------------------------------------------- |
| Stock Service   | 1099 | `rmi://stock-service:1099/StockService`     |
| Ventes Service  | 1100 | `rmi://ventes-service:1100/VentesService`   |
| Finance Service | 1101 | `rmi://finance-service:1101/FinanceService` |

#### Interfaces RMI

- **StockServiceRmi** : Gestion du stock (quantités, réservations, disponibilité)
- **VentesServiceRmi** : Gestion des commandes (création, statuts, montants)
- **FinanceServiceRmi** : Gestion financière (factures, paiements, soldes)

#### Avantages de RMI

✅ Communication synchrone fortement typée  
✅ Haute performance (appels directs sans sérialisation JSON)  
✅ Complément idéal à REST pour les appels inter-services  
✅ Complément à RabbitMQ pour la communication asynchrone  
✅ Coexiste avec les protocoles existants (REST + RabbitMQ)

#### Exemple d'utilisation

```java
@Service
public class OrderService {

    private final StockServiceRmi stockServiceRmi;

    @Autowired(required = false)
    public OrderService(StockServiceRmi stockServiceRmi) {
        this.stockServiceRmi = stockServiceRmi;
    }

    public void createOrder(Long productId, Integer quantity) {
        try {
            // Vérifier la disponibilité via RMI
            Boolean available = stockServiceRmi.isProductAvailable(productId, quantity);
            if (available) {
                // Créer la commande...
            }
        } catch (RemoteException e) {
            // Gérer les erreurs RMI
        }
    }
}
```

📖 Pour plus de détails, voir [RMI_GUIDE.md](./RMI_GUIDE.md)

---

## �🔧 Services du projet

### 1. **API Gateway** (Port 8080)

**Rôle** : Point d'entrée unique pour tous les clients  
**Type** : Spring Cloud Gateway  
**Responsabilités** :

- Redirection des requêtes vers les services appropriés
- Gestion du CORS (Cross-Origin Resource Sharing)
- Configuration des routes avec préfixe
- Équilibrage de charge (si nécessaire)

**Configuration des routes** :

```yaml
/api/stock/**     → http://stock-service:8081/produits
/api/ventes/**    → http://ventes-service:8082/commandes
/api/finance/**   → http://finance-service:8083/factures
```

**Dépendances principales** :

- Spring Cloud Gateway
- Spring Boot Actuator

---

### 2. **Stock Service** (Port 8081)

**Rôle** : Gestion de l'inventaire des produits  
**Type** : Microservice REST  
**Base de données** : PostgreSQL `stock_db` (port 5433)

#### 🔹 Responsabilités

- Créer, lire, mettre à jour, supprimer des produits
- Gérer les entrées et sorties de stock
- Alerter en cas de stock faible
- Exposer les produits via API REST

#### 🔹 Modèle de données

```
Produit {
  id: Long (PK)
  reference: String (Unique) - Code produit
  nom: String
  prixUnitaire: BigDecimal
  quantiteEnStock: Integer
  seuilAlerte: Integer - Niveau minimum d'alerte
  enAlerte: Boolean
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

#### 🔹 Endpoints

| Méthode | Endpoint                    | Description              |
| ------- | --------------------------- | ------------------------ |
| GET     | `/produits`                 | Lister tous les produits |
| GET     | `/produits/{id}`            | Obtenir un produit       |
| GET     | `/produits/reference/{ref}` | Chercher par référence   |
| GET     | `/produits/alertes`         | Produits en alerte stock |
| POST    | `/produits`                 | Créer un produit         |
| PUT     | `/produits/{id}`            | Modifier un produit      |
| POST    | `/produits/{id}/entree`     | Ajouter du stock         |
| POST    | `/produits/{id}/sortie`     | Retirer du stock         |
| DELETE  | `/produits/{id}`            | Supprimer un produit     |

#### 🔹 Exemple de requête

```bash
# Créer un produit
curl -X POST http://localhost:8080/api/stock/produits \
  -H "Content-Type: application/json" \
  -d '{
    "reference": "LAPTOP-001",
    "nom": "Laptop Dell XPS 15",
    "prixUnitaire": 2500.00,
    "quantiteEnStock": 50,
    "seuilAlerte": 5
  }'

# Ajouter du stock
curl -X POST http://localhost:8080/api/stock/produits/1/entree \
  -H "Content-Type: application/json" \
  -d '{"quantite": 10}'

# Retirer du stock (vente)
curl -X POST http://localhost:8080/api/stock/produits/1/sortie \
  -H "Content-Type: application/json" \
  -d '{"quantite": 2, "motif": "Vente"}'
```

#### 🔹 Technologies

- Spring Boot 3.2.5
- Spring Data JPA (ORM)
- Spring AMQP (RabbitMQ)
- PostgreSQL Driver
- Lombok (annotations)

---

### 3. **Ventes Service** (Port 8082)

**Rôle** : Gestion des commandes clients  
**Type** : Microservice REST  
**Base de données** : PostgreSQL `ventes_db` (port 5434)

#### 🔹 Responsabilités

- Créer et gérer les commandes
- Valider les stocks auprès du Stock Service (synchrone)
- Décrémenter le stock automatiquement
- Publier des événements pour facturation (asynchrone)
- Changer le statut des commandes

#### 🔹 Modèle de données

```
Commande {
  id: Long (PK)
  client: String
  statut: Enum (CREEE, CONFIRMEE, LIVREE, ANNULEE)
  dateCreation: LocalDateTime
  lignes: List<LigneCommande>
}

LigneCommande {
  id: Long (PK)
  commande_id: Long (FK)
  produitReference: String
  quantite: Integer
  prixUnitaire: BigDecimal
  total: BigDecimal
}
```

#### 🔹 Endpoints

| Méthode | Endpoint                    | Description                 |
| ------- | --------------------------- | --------------------------- |
| GET     | `/commandes`                | Lister toutes les commandes |
| GET     | `/commandes/{id}`           | Détail d'une commande       |
| POST    | `/commandes`                | Créer une commande          |
| PATCH   | `/commandes/{id}/confirmer` | Confirmer la commande       |
| PATCH   | `/commandes/{id}/livrer`    | Marquer comme livrée        |
| PATCH   | `/commandes/{id}/annuler`   | Annuler la commande         |
| DELETE  | `/commandes/{id}`           | Supprimer la commande       |

#### 🔹 Flux de création de commande

```
1. Client crée une commande (POST /commandes)
   ↓
2. Ventes Service valide les produits
   ↓
3. Appel Feign synchrone → Stock Service
   - Vérifie les quantités disponibles
   - Récupère les prix
   ↓
4. Stock Service retourne les détails (ou erreur)
   ↓
5. Si OK → Créer la commande en BD + Décrémenter stock
   ↓
6. Publier événement "commande.created" → RabbitMQ
   ↓
7. Finance Service écoute et crée la facture
```

#### 🔹 Exemple de requête

```bash
# Créer une commande
curl -X POST http://localhost:8080/api/ventes/commandes \
  -H "Content-Type: application/json" \
  -d '{
    "client": "Société XYZ",
    "lignes": [
      {
        "produitReference": "LAPTOP-001",
        "quantite": 2
      }
    ]
  }'

# Confirmer une commande
curl -X PATCH http://localhost:8080/api/ventes/commandes/1/confirmer

# Annuler une commande
curl -X PATCH http://localhost:8080/api/ventes/commandes/1/annuler
```

#### 🔹 Technologies

- Spring Boot 3.2.5
- Spring Cloud OpenFeign (appels REST synchrones)
- Spring AMQP (publication d'événements)
- PostgreSQL
- Lombok

#### 🔹 Client Feign (Stock Service)

```java
@FeignClient(name = "stock-service", url = "${stock.service.url}")
public interface StockClient {
    @GetMapping("/produits/reference/{ref}")
    ProduitResponse getProduitByReference(@PathVariable String ref);

    @PostMapping("/produits/{id}/sortie")
    ProduitResponse sortieStock(@PathVariable Long id,
                                @RequestBody MouvementRequest req);
}
```

---

### 4. **Finance Service** (Port 8083)

**Rôle** : Gestion des factures et des paiements  
**Type** : Microservice REST  
**Base de données** : PostgreSQL `finance_db` (port 5435)

#### 🔹 Responsabilités

- Écouter les événements de création de commande (RabbitMQ)
- Créer automatiquement les factures
- Enregistrer les paiements
- Générer un tableau de bord financier
- Marquer les factures en retard

#### 🔹 Modèle de données

```
Facture {
  id: Long (PK)
  commandeId: Long - Référence à la commande
  montantTotal: BigDecimal
  montantPaye: BigDecimal
  statut: Enum (EN_ATTENTE, PARTIELLEMENT_PAYEE, PAYEE, EN_RETARD)
  dateCreation: LocalDateTime
  dateEchéance: LocalDate
  paiements: List<Paiement>
}

Paiement {
  id: Long (PK)
  facture_id: Long (FK)
  montant: BigDecimal
  mode: String (CARTE, VIREMENT, CHEQUE, etc.)
  datePaiement: LocalDateTime
}
```

#### 🔹 Endpoints

| Méthode | Endpoint                   | Description                |
| ------- | -------------------------- | -------------------------- |
| GET     | `/factures`                | Lister toutes les factures |
| GET     | `/factures/{id}`           | Détail d'une facture       |
| GET     | `/factures/dashboard`      | Tableau de bord financier  |
| POST    | `/factures/{id}/paiements` | Enregistrer un paiement    |
| POST    | `/factures/marquer-retard` | Marquer les retards        |

#### 🔹 Flux automatique

```
RabbitMQ émet "commande.created"
            ↓
@RabbitListener intercepte l'événement
            ↓
Finance Service reçoit :
  - ID commande
  - Client
  - Montant total
  - Lignes (produits + quantités + prix)
            ↓
Crée une Facture en BD
            ↓
Statut initial = EN_ATTENTE
```

#### 🔹 Exemple de requête

```bash
# Lister les factures
curl -X GET http://localhost:8080/api/finance/factures

# Tableau de bord
curl -X GET http://localhost:8080/api/finance/factures/dashboard

# Enregistrer un paiement
curl -X POST http://localhost:8080/api/finance/factures/1/paiements \
  -H "Content-Type: application/json" \
  -d '{
    "montant": 5000.00,
    "mode": "VIREMENT"
  }'

# Marquer les factures en retard
curl -X POST http://localhost:8080/api/finance/factures/marquer-retard
```

#### 🔹 Technologies

- Spring Boot 3.2.5
- Spring Data JPA (ORM)
- Spring AMQP (consommation d'événements)
- PostgreSQL
- Lombok

---

### 5. **Frontend** (Port 3000)

**Rôle** : Interface utilisateur web  
**Type** : SPA React  
**Technologie** : TypeScript + React 18 + Vite

#### 🔹 Structure

```
frontend/
├── src/
│   ├── api/client.ts          # Client API centralisé (Axios)
│   ├── pages/
│   │   ├── Dashboard.tsx      # Tableau de bord
│   │   ├── Stock.tsx          # Gestion des produits
│   │   ├── Sales.tsx          # Gestion des commandes
│   │   └── Finance.tsx        # Gestion des factures
│   ├── hooks/
│   │   └── useAsync.ts        # Hook pour appels async
│   ├── components/
│   │   └── ErrorBoundary.tsx  # Gestion des erreurs
│   ├── App.tsx                # Routeur principal
│   └── main.tsx               # Point d'entrée
```

#### 🔹 Client API (Axios)

```typescript
const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: { 'Content-Type': 'application/json' }
})

// Stock API
export const stockAPI = {
    getProducts: () => apiClient.get('/stock/produits'),
    createProduct: (data) => apiClient.post('/stock/produits', data),
    addStock: (id, quantity) => apiClient.post(`/stock/produits/${id}/entree`, ...),
    removeStock: (id, quantity) => apiClient.post(`/stock/produits/${id}/sortie`, ...)
}

// Sales API
export const salesAPI = {
    getOrders: () => apiClient.get('/ventes/commandes'),
    createOrder: (data) => apiClient.post('/ventes/commandes', data),
    confirmOrder: (id) => apiClient.patch(`/ventes/commandes/${id}/confirmer`)
}

// Finance API
export const financeAPI = {
    getInvoices: () => apiClient.get('/finance/factures'),
    getDashboard: () => apiClient.get('/finance/factures/dashboard'),
    recordPayment: (id, data) => apiClient.post(`/finance/factures/${id}/paiements`, data)
}
```

#### 🔹 Pages principales

- **Dashboard** : Vue d'ensemble avec statistiques clés
- **Stock** : CRUD pour les produits, gestion des entrées/sorties
- **Sales** : Liste des commandes avec statuts, création de nouvelles commandes
- **Finance** : Factures, paiements, tableau de bord financier

#### 🔹 Technologies

- React 18.2
- TypeScript 5.3
- Vite 5.0
- Axios 1.6
- React Router 6.20
- Tailwind CSS 3.4
- Lucide React (icônes)

---

## 🔗 Communication inter-services

### 1. Communication Synchrone (REST + Feign)

**Utilisée entre** : Ventes Service ↔ Stock Service

**Caractéristiques** :

- Appels HTTP REST en temps réel
- Request/Response immédiat
- Utilisée lors de la création d'une commande

**Flux** :

```
Client → API Gateway → Ventes Controller → VentesService
                                              ↓
                                          StockClient (Feign)
                                              ↓
                                          Stock Service REST
                                              ↓
                                          StockController
                                              ↓
                                          StockService
                                              ↓ Retour
                                          Response (Product Details)
```

**Exemple de code** :

```java
// Dans VentesService.creerCommande()
for (LigneCommande ligne : lignes) {
    // Appel Feign synchrone
    StockClient.ProduitResponse produit = stockClient
        .getProduitByReference(ligne.getProduitReference());

    // Vérifier le stock
    if (produit.getQuantiteEnStock() < ligne.getQuantite()) {
        throw new InsufficientStockException(...);
    }

    // Décrémenter le stock
    stockClient.sortieStock(produit.getId(),
        new MouvementRequest(ligne.getQuantite(), "Vente"));
}
```

---

### 2. Communication Asynchrone (RabbitMQ)

**Utilisée entre** : Ventes Service → Finance Service

**Caractéristiques** :

- Basée sur les événements (Event-Driven)
- Publication/Souscription
- Découplage entre services
- Traitement en arrière-plan

#### 🔹 Configuration RabbitMQ

```yaml
# Exchange
Name: erp.exchange
Type: Topic
Durable: true

# Queue
Name: finance.commande.queue
Binding: erp.exchange avec pattern "commande.created"
```

#### 🔹 Publication (Ventes Service)

```java
@Component
public class VentesEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishCommandeCreated(Commande commande) {
        CommandeEvent event = new CommandeEvent(
            commande.getId(),
            commande.getClient(),
            commande.getTotal(),
            commande.getLignes()
        );

        rabbitTemplate.convertAndSend(
            "erp.exchange",           // Exchange
            "commande.created",       // Routing Key
            event                     // Message
        );
    }
}
```

#### 🔹 Consommation (Finance Service)

```java
@Component
public class FinanceEventConsumer {

    @RabbitListener(queues = "finance.commande.queue")
    public void handleCommandeCreated(CommandeEvent event) {
        // Créer une facture basée sur l'événement
        Facture facture = new Facture();
        facture.setCommandeId(event.getCommandeId());
        facture.setClient(event.getClient());
        facture.setMontantTotal(event.getMontantTotal());
        facture.setStatut(StatutFacture.EN_ATTENTE);

        factureRepository.save(facture);
        logger.info("Facture créée pour commande: " + event.getCommandeId());
    }
}
```

---

## 💾 Base de données

### Architecture des données

Chaque service a sa **propre base de données indépendante** (principe de "Database per Service").

#### 📊 Stock DB

```
Hôte: localhost:5433
BD: stock_db
User: erp
Password: erp_secret

Tableaux:
├── produit
│   ├── id (PK)
│   ├── reference (UNIQUE)
│   ├── nom
│   ├── prix_unitaire
│   ├── quantite_en_stock
│   ├── seuil_alerte
│   ├── en_alerte
│   ├── created_at
│   └── updated_at
```

#### 📊 Ventes DB

```
Hôte: localhost:5434
BD: ventes_db
User: erp
Password: erp_secret

Tableaux:
├── commande
│   ├── id (PK)
│   ├── client
│   ├── statut
│   ├── date_creation
│   └── updated_at
├── ligne_commande
│   ├── id (PK)
│   ├── commande_id (FK)
│   ├── produit_reference
│   ├── quantite
│   ├── prix_unitaire
│   └── total
```

#### 📊 Finance DB

```
Hôte: localhost:5435
BD: finance_db
User: erp
Password: erp_secret

Tableaux:
├── facture
│   ├── id (PK)
│   ├── commande_id
│   ├── client
│   ├── montant_total
│   ├── montant_paye
│   ├── statut
│   ├── date_creation
│   ├── date_echeance
│   └── updated_at
├── paiement
│   ├── id (PK)
│   ├── facture_id (FK)
│   ├── montant
│   ├── mode
│   └── date_paiement
```

---

## 🔌 API Endpoints

### Accès par Gateway (Port 8080)

#### **Stock Service** → `GET/POST/PUT/DELETE /api/stock/produits`

```
GET    /api/stock/produits              → Tous les produits
GET    /api/stock/produits/{id}         → Un produit
GET    /api/stock/produits/reference/{ref}  → Par référence
GET    /api/stock/produits/alertes      → Alertes stock
POST   /api/stock/produits              → Créer
PUT    /api/stock/produits/{id}         → Modifier
POST   /api/stock/produits/{id}/entree  → Ajouter stock
POST   /api/stock/produits/{id}/sortie  → Retirer stock
DELETE /api/stock/produits/{id}         → Supprimer
```

#### **Ventes Service** → `GET/POST/PATCH/DELETE /api/ventes/commandes`

```
GET    /api/ventes/commandes            → Toutes les commandes
GET    /api/ventes/commandes/{id}       → Une commande
POST   /api/ventes/commandes            → Créer
PATCH  /api/ventes/commandes/{id}/confirmer  → Confirmer
PATCH  /api/ventes/commandes/{id}/livrer     → Livrer
PATCH  /api/ventes/commandes/{id}/annuler    → Annuler
DELETE /api/ventes/commandes/{id}       → Supprimer
```

#### **Finance Service** → `GET/POST /api/finance/factures`

```
GET    /api/finance/factures            → Toutes les factures
GET    /api/finance/factures/{id}       → Une facture
GET    /api/finance/factures/dashboard  → Tableau de bord
POST   /api/finance/factures/{id}/paiements    → Paiement
POST   /api/finance/factures/marquer-retard    → Retards
```

---

## 🔄 Flux de données

### Flux 1 : Créer un produit

```
Frontend (Stock.tsx)
  └─ POST /api/stock/produits {reference, nom, prixUnitaire, ...}
     │
API Gateway
     │
Stock Service:8081
  └─ POST /produits
     │
StockController.create(ProduitDto.Request)
     │
StockService.create()
     │
ProduitRepository.save()
     │
PostgreSQL (stock_db)
     │
← Response 201 Created avec l'objet créé
     │
Frontend reçoit le produit
```

---

### Flux 2 : Créer une commande (Synchrone)

```
Frontend (Sales.tsx)
  └─ POST /api/ventes/commandes
     { client: "ABC", lignes: [{produitReference: "LAPTOP-001", quantite: 2}] }
     │
API Gateway
     │
Ventes Service:8082
  └─ POST /commandes
     │
VentesController.create(CommandeDto.Request)
     │
VentesService.creerCommande()
     │
     ├─ Valider les lignes
     │
     ├─ Pour chaque ligne:
     │  └─ StockClient.getProduitByReference("LAPTOP-001")  [FEIGN - SYNC]
     │     │
     │     Stock Service:8081
     │     └─ GET /produits/reference/LAPTOP-001
     │        │
     │        StockController.getByReference()
     │        │
     │        StockService.findByReference()
     │        │
     │        PostgreSQL (stock_db)
     │        │
     │        ← ProduitResponse
     │
     ├─ Vérifier disponibilité en stock
     │
     ├─ Si OK:
     │  └─ StockClient.sortieStock(produitId, quantite)  [FEIGN - SYNC]
     │     │
     │     Stock Service → Décrémenter quantiteEnStock
     │     │
     │     PostgreSQL UPDATE (stock_db)
     │
     ├─ Créer la Commande en BD
     │  └─ CommandeRepository.save()
     │     │
     │     PostgreSQL INSERT (ventes_db)
     │
     ├─ Publier événement "commande.created"  [ASYNC - RabbitMQ]
     │  └─ RabbitTemplate.convertAndSend(exchange, routing_key, event)
     │     │
     │     RabbitMQ (erp.exchange topic)
     │
     ├─ Finance Service écoute  [ASYNC]
     │  └─ @RabbitListener(queues = "finance.commande.queue")
     │     │
     │     FinanceEventConsumer.handleCommandeCreated()
     │     │
     │     Créer Facture (Finance DB)
     │
← Response 201 Created avec la commande
     │
Frontend reçoit la commande + Facture créée en arrière-plan
```

---

### Flux 3 : Enregistrer un paiement

```
Frontend (Finance.tsx)
  └─ POST /api/finance/factures/1/paiements
     { montant: 5000.00, mode: "VIREMENT" }
     │
API Gateway
     │
Finance Service:8083
  └─ POST /factures/1/paiements
     │
FinanceController.payer()
     │
FinanceService.enregistrerPaiement()
     │
     ├─ Récupérer la Facture
     │  └─ FactureRepository.findById(1)
     │
     ├─ Créer un Paiement
     │  └─ PaiementRepository.save()
     │     │
     │     PostgreSQL INSERT (finance_db.paiement)
     │
     ├─ Mettre à jour Facture.montantPaye
     │  └─ FactureRepository.save()
     │     │
     │     PostgreSQL UPDATE (finance_db.facture)
     │
     ├─ Vérifier statut:
     │  ├─ Si montantPaye == montantTotal → PAYEE
     │  ├─ Si montantPaye > 0 → PARTIELLEMENT_PAYEE
     │
← Response 201 Created avec le Paiement
     │
Frontend affiche le paiement enregistré
```

---

## 🛠️ Technologies utilisées

### Backend

| Technologie     | Version     | Usage                |
| --------------- | ----------- | -------------------- |
| Java            | 21          | Langage principal    |
| Spring Boot     | 3.2.5       | Framework Web        |
| Spring Cloud    | 2023.0.1    | Gateway + OpenFeign  |
| Spring Data JPA | 3.2.5       | ORM (Hibernate)      |
| Spring AMQP     | 3.2.5       | RabbitMQ integration |
| PostgreSQL      | 16 (Alpine) | Base de données      |
| RabbitMQ        | 3.13        | Message broker       |
| Lombok          | latest      | Annotations          |
| Maven           | 3.x         | Build tool           |
| Docker          | latest      | Containerization     |

### Frontend

| Technologie  | Version | Usage       |
| ------------ | ------- | ----------- |
| React        | 18.2    | UI Library  |
| TypeScript   | 5.3     | Type safety |
| Vite         | 5.0     | Build tool  |
| Axios        | 1.6     | HTTP client |
| React Router | 6.20    | Routing     |
| Tailwind CSS | 3.4     | Styling     |
| Lucide React | 0.294   | Icons       |
| Node.js      | 18+     | Runtime     |

### Infrastructure

| Composant         | Details                     |
| ----------------- | --------------------------- |
| Container Runtime | Docker                      |
| Orchestration     | Docker Compose              |
| Networks          | Bridge network `erp-net`    |
| Volumes           | Named volumes (persistence) |

---

## 🚀 Diagramme de déploiement

### Architecture de déploiement avec Docker Compose

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DOCKER HOST (localhost)                      │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │           Bridge Network: erp-net (172.18.0.0/16)           │  │
│  │                                                              │  │
│  │  ┌─────────────────────────────────────────────────────┐   │  │
│  │  │ FRONTEND LAYER                                      │   │  │
│  │  │  ┌───────────────────────────────────────────────┐ │   │  │
│  │  │  │ Frontend Container                            │ │   │  │
│  │  │  │ Image: node:18-alpine                         │ │   │  │
│  │  │  │ Port Container: 3000                          │ │   │  │
│  │  │  │ Port Host: 3000 → Container 3000             │ │   │  │
│  │  │  │ Volume: . → /app                             │ │   │  │
│  │  │  │ Env: VITE_API_URL=http://localhost:8080/api  │ │   │  │
│  │  │  └───────────────────────────────────────────────┘ │   │  │
│  │  └─────────────────────────────────────────────────────┘   │  │
│  │                              │                             │  │
│  │                    HTTP REST (Axios)                       │  │
│  │                              ▼                             │  │
│  │  ┌─────────────────────────────────────────────────────┐   │  │
│  │  │ GATEWAY LAYER                                       │   │  │
│  │  │  ┌───────────────────────────────────────────────┐ │   │  │
│  │  │  │ API Gateway Container                         │ │   │  │
│  │  │  │ Image: openjdk:21-jdk-slim                   │ │   │  │
│  │  │  │ Port Container: 8080                         │ │   │  │
│  │  │  │ Port Host: 8080 → Container 8080            │ │   │  │
│  │  │  │ Build: ./api-gateway/Dockerfile              │ │   │  │
│  │  │  │ Env: SPRING_PROFILES_ACTIVE=docker           │ │   │  │
│  │  │  └───────────────────────────────────────────────┘ │   │  │
│  │  └─────────────────────────────────────────────────────┘   │  │
│  │         │              │              │                    │  │
│  │         │              │              │                    │  │
│  │         ▼              ▼              ▼                    │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐      │  │
│  │  │ SERVICES LAYER                                   │      │  │
│  │  │              │ │              │ │              │      │  │
│  │  │  ┌─────────┐ │ │ ┌─────────┐ │ │ ┌─────────┐ │      │  │
│  │  │  │ Stock   │ │ │ │ Ventes  │ │ │ │ Finance │ │      │  │
│  │  │  │ :8081   │ │ │ │ :8082   │ │ │ │ :8083   │ │      │  │
│  │  │  │ 8081    │ │ │ │ 8082    │ │ │ │ 8083    │ │      │  │
│  │  │  │ 8081    │ │ │ │ 8082    │ │ │ │ 8083    │ │      │  │
│  │  │  └─────────┘ │ │ └─────────┘ │ │ └─────────┘ │      │  │
│  │  │              │ │              │ │              │      │  │
│  │  │ Spring Boot  │ │ Spring Boot  │ │ Spring Boot  │      │  │
│  │  │ 3.2.5 / J21  │ │ 3.2.5 / J21  │ │ 3.2.5 / J21  │      │  │
│  │  │ Depends:     │ │ Depends:     │ │ Depends:     │      │  │
│  │  │ - stock-db   │ │ - ventes-db  │ │ - finance-db │      │  │
│  │  │ - rabbitmq   │ │ - rabbitmq   │ │ - rabbitmq   │      │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘      │  │
│  │         │ Feign         │                                 │  │
│  │         │ (Sync)        │                                 │  │
│  │         └───────────────┘                                 │  │
│  │                         │                                 │  │
│  │                 RabbitMQ (Async Events)                   │  │
│  │                         ▼                                 │  │
│  │  ┌─────────────────────────────────────────────────────┐   │  │
│  │  │ MESSAGE BROKER LAYER                               │   │  │
│  │  │  ┌───────────────────────────────────────────────┐ │   │  │
│  │  │  │ RabbitMQ Container                            │ │   │  │
│  │  │  │ Image: rabbitmq:3.13-management-alpine       │ │   │  │
│  │  │  │ Port Container: 5672 (AMQP), 15672 (Mgmt)   │ │   │  │
│  │  │  │ Port Host: 5672 → 5672, 15672 → 15672       │ │   │  │
│  │  │  │ Env:                                         │ │   │  │
│  │  │  │  - RABBITMQ_DEFAULT_USER: erp               │ │   │  │
│  │  │  │  - RABBITMQ_DEFAULT_PASS: erp_secret        │ │   │  │
│  │  │  │ Management UI: http://localhost:15672       │ │   │  │
│  │  │  └───────────────────────────────────────────────┘ │   │  │
│  │  └─────────────────────────────────────────────────────┘   │  │
│  │         │ (JDBC)         │ (JDBC)         │ (JDBC)         │  │
│  │         ▼                ▼                ▼                │  │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐      │  │
│  │  │DATABASE LAYER                                    │      │  │
│  │  │              │ │              │ │              │      │  │
│  │  │ PostgreSQL   │ │ PostgreSQL   │ │ PostgreSQL   │      │  │
│  │  │ :5433        │ │ :5434        │ │ :5435        │      │  │
│  │  │ 5432 → 5433  │ │ 5432 → 5434  │ │ 5432 → 5435  │      │  │
│  │  │ DB: stock_db │ │ DB: ventes_db│ │ DB:finance_db│      │  │
│  │  │ User: erp    │ │ User: erp    │ │ User: erp    │      │  │
│  │  │ Pass: erp*   │ │ Pass: erp*   │ │ Pass: erp*   │      │  │
│  │  │              │ │              │ │              │      │  │
│  │  │ Vol: stock-  │ │ Vol: ventes- │ │ Vol: finance-│      │  │
│  │  │ data         │ │ data         │ │ data         │      │  │
│  │  └──────────────┘ └──────────────┘ └──────────────┘      │  │
│  │         │                │                │               │  │
│  │  ┌──────┴────────────────┴────────────────┴─────┐        │  │
│  │  │                                              │        │  │
│  │  │  STORAGE LAYER (Named Volumes)              │        │  │
│  │  │  ┌────────────┐  ┌────────────┐  ┌────────┐│        │  │
│  │  │  │ stock-data │  │ ventes-data│  │finance-││        │  │
│  │  │  │ (/var/lib) │  │ (/var/lib) │  │data    ││        │  │
│  │  │  └────────────┘  └────────────┘  └────────┘│        │  │
│  │  │                                              │        │  │
│  │  └──────────────────────────────────────────────┘        │  │
│  │                                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📋 Détails du déploiement

### Couches de déploiement

#### 1️⃣ **Frontend Layer** (Port 3000)

```yaml
Service: erp-frontend
Image: node:18-alpine
Container Port: 3000
Host Port: 3000
Volumes:
  - ./frontend:/app # Code source
Build Command: npm run dev # Vite dev server
Network: erp-net
Env Variables:
  VITE_API_URL: http://localhost:8080/api
```

#### 2️⃣ **Gateway Layer** (Port 8080)

```yaml
Service: api-gateway
Build: ./api-gateway/Dockerfile
Base Image: openjdk:21-jdk-slim
Container Port: 8080
Host Port: 8080
Container Name: api-gateway
Network: erp-net
Depends On: [stock-service, ventes-service, finance-service]
Env Variables:
  SPRING_PROFILES_ACTIVE: docker
  SPRING_APPLICATION_NAME: api-gateway
```

#### 3️⃣ **Microservices Layer** (Ports 8081-8083)

**Stock Service (Port 8081)**

```yaml
Service: stock-service
Build: ./stock-service/Dockerfile
Base Image: openjdk:21-jdk-slim
Container Port: 8081
Host Port: 8081
Network: erp-net
Depends On: [stock-db, rabbitmq]
Env Variables:
  SPRING_DATASOURCE_URL: jdbc:postgresql://stock-db:5432/stock_db
  SPRING_DATASOURCE_USERNAME: erp
  SPRING_DATASOURCE_PASSWORD: erp_secret
  SPRING_RABBITMQ_HOST: rabbitmq
  SPRING_RABBITMQ_USERNAME: erp
  SPRING_RABBITMQ_PASSWORD: erp_secret
```

**Ventes Service (Port 8082)**

```yaml
Service: ventes-service
Build: ./ventes-service/Dockerfile
Base Image: openjdk:21-jdk-slim
Container Port: 8082
Host Port: 8082
Network: erp-net
Depends On: [ventes-db, rabbitmq, stock-service]
Env Variables:
  SPRING_DATASOURCE_URL: jdbc:postgresql://ventes-db:5432/ventes_db
  SPRING_DATASOURCE_USERNAME: erp
  SPRING_DATASOURCE_PASSWORD: erp_secret
  SPRING_RABBITMQ_HOST: rabbitmq
  SPRING_RABBITMQ_USERNAME: erp
  SPRING_RABBITMQ_PASSWORD: erp_secret
  STOCK_SERVICE_URL: http://stock-service:8081
```

**Finance Service (Port 8083)**

```yaml
Service: finance-service
Build: ./finance-service/Dockerfile
Base Image: openjdk:21-jdk-slim
Container Port: 8083
Host Port: 8083
Network: erp-net
Depends On: [finance-db, rabbitmq]
Env Variables:
  SPRING_DATASOURCE_URL: jdbc:postgresql://finance-db:5432/finance_db
  SPRING_DATASOURCE_USERNAME: erp
  SPRING_DATASOURCE_PASSWORD: erp_secret
  SPRING_RABBITMQ_HOST: rabbitmq
  SPRING_RABBITMQ_USERNAME: erp
  SPRING_RABBITMQ_PASSWORD: erp_secret
```

#### 4️⃣ **Message Broker Layer** (Ports 5672, 15672)

```yaml
Service: rabbitmq
Image: rabbitmq:3.13-management-alpine
Container Port: 5672 (AMQP Protocol)
Container Port: 15672 (Management UI)
Host Port: 5672 → Container 5672
Host Port: 15672 → Container 15672
Container Name: rabbitmq
Network: erp-net
Env Variables:
  RABBITMQ_DEFAULT_USER: erp
  RABBITMQ_DEFAULT_PASS: erp_secret
Management UI: http://localhost:15672
  Login: erp / erp_secret
```

#### 5️⃣ **Database Layer** (Ports 5433-5435)

**Stock DB (Port 5433)**

```yaml
Service: stock-db
Image: postgres:16-alpine
Container Port: 5432
Host Port: 5433
Container Name: stock-db
Network: erp-net
Volume: stock-data:/var/lib/postgresql/data
Env Variables:
  POSTGRES_DB: stock_db
  POSTGRES_USER: erp
  POSTGRES_PASSWORD: erp_secret
Connection String:
  - Internal (Docker): jdbc:postgresql://stock-db:5432/stock_db
  - External (Host): postgresql://localhost:5433/stock_db
```

**Ventes DB (Port 5434)**

```yaml
Service: ventes-db
Image: postgres:16-alpine
Container Port: 5432
Host Port: 5434
Container Name: ventes-db
Network: erp-net
Volume: ventes-data:/var/lib/postgresql/data
Env Variables:
  POSTGRES_DB: ventes_db
  POSTGRES_USER: erp
  POSTGRES_PASSWORD: erp_secret
Connection String:
  - Internal (Docker): jdbc:postgresql://ventes-db:5432/ventes_db
  - External (Host): postgresql://localhost:5434/ventes_db
```

**Finance DB (Port 5435)**

```yaml
Service: finance-db
Image: postgres:16-alpine
Container Port: 5432
Host Port: 5435
Container Name: finance-db
Network: erp-net
Volume: finance-data:/var/lib/postgresql/data
Env Variables:
  POSTGRES_DB: finance_db
  POSTGRES_USER: erp
  POSTGRES_PASSWORD: erp_secret
Connection String:
  - Internal (Docker): jdbc:postgresql://finance-db:5432/finance_db
  - External (Host): postgresql://localhost:5435/finance_db
```

### Réseau et Volume

#### 🌐 Network Configuration

```yaml
Network Name: erp-net
Driver: bridge
IP Range: 172.18.0.0/16
Services connectés:
  - api-gateway       (172.18.0.2)
  - stock-service     (172.18.0.3)
  - ventes-service    (172.18.0.4)
  - finance-service   (172.18.0.5)
  - rabbitmq          (172.18.0.6)
  - stock-db          (172.18.0.7)
  - ventes-db         (172.18.0.8)
  - finance-db        (172.18.0.9)
  - erp-frontend      (172.18.0.10)

Communication: Tous les services communiquent via le DNS du réseau
Exemple: stock-service peut accéder à ventes-db via "ventes-db:5432"
```

#### 💾 Volumes (Persistence)

```yaml
Volumes:
  stock-data:
    Driver: local
    Mount Path: /var/lib/postgresql/data
    Description: Données persistantes PostgreSQL (Stock)

  ventes-data:
    Driver: local
    Mount Path: /var/lib/postgresql/data
    Description: Données persistantes PostgreSQL (Ventes)

  finance-data:
    Driver: local
    Mount Path: /var/lib/postgresql/data
    Description: Données persistantes PostgreSQL (Finance)

Avantages:
  - Données persistées même après arrêt des conteneurs
  - Données partagées entre redémarrages
  - Isolation des données par service
  - Pas de dépendance au système de fichiers host
```

---

## 🚀 Démarrage du projet

### Prérequis

- Docker & Docker Compose
- Node.js 18+ (pour le frontend local)
- Java 21 (optionnel, Docker le fournit)

### Option 1 : Avec Docker Compose (recommandé)

```bash
cd /c/Users/ADMIN/mini_erp
docker compose up --build

# Services disponibles:
# Frontend:    http://localhost:3000 (attention: remplacé par le Vite dev server)
# API Gateway: http://localhost:8080
# Stock:       http://localhost:8081 (direct)
# Ventes:      http://localhost:8082 (direct)
# Finance:     http://localhost:8083 (direct)
# RabbitMQ UI: http://localhost:15672 (user:erp, pass:erp_secret)

# Bases de données (direct):
# Stock DB:    localhost:5433
# Ventes DB:   localhost:5434
# Finance DB:  localhost:5435
```

### Option 2 : Frontend local + Docker pour backend

```bash
# Terminal 1 - Backend
docker compose up --build stock-db ventes-db finance-db rabbitmq
docker compose up api-gateway stock-service ventes-service finance-service

# Terminal 2 - Frontend
cd frontend
npm install
npm run dev

# Frontend: http://localhost:3000
```

---

## 📋 Résumé architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Mini-ERP Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  FRONTEND (React/TypeScript) — Port 3000                       │
│  └─ UI pour Stock, Ventes, Finance                             │
│                                                                 │
│  API GATEWAY (Spring Cloud) — Port 8080                        │
│  └─ Single entry point                                         │
│  └─ Routes vers 3 services                                     │
│                                                                 │
│  SERVICES (3 × Spring Boot):                                   │
│  ├─ Stock Service — Port 8081                                  │
│  │  └─ Gestion inventaire                                      │
│  │  └─ BD: PostgreSQL :5433                                    │
│  │                                                              │
│  ├─ Ventes Service — Port 8082                                 │
│  │  └─ Gestion commandes                                       │
│  │  └─ Appels Feign (Sync) vers Stock                          │
│  │  └─ Publie événements (Async) RabbitMQ                      │
│  │  └─ BD: PostgreSQL :5434                                    │
│  │                                                              │
│  └─ Finance Service — Port 8083                                │
│     └─ Gestion factures/paiements                              │
│     └─ Écoute événements (Async) RabbitMQ                      │
│     └─ BD: PostgreSQL :5435                                    │
│                                                                 │
│  MESSAGE BROKER:                                               │
│  └─ RabbitMQ — Port 5672 (AMQP)                               │
│  └─ Management UI — Port 15672                                │
│  └─ Exchange: erp.exchange (Topic)                            │
│  └─ Routing: commande.created → Finance                       │
│                                                                 │
│  COMMUNICATIONS:                                               │
│  ├─ REST (Gateway) : Frontend ↔ Gateway ↔ Services           │
│  ├─ Feign (REST Sync) : Ventes → Stock                        │
│  └─ RabbitMQ (Async) : Ventes → Finance                       │
│                                                                 │
│  DATABASE per Service Pattern:                                 │
│  ├─ Stock DB (stock_db) — Produits                             │
│  ├─ Ventes DB (ventes_db) — Commandes                          │
│  └─ Finance DB (finance_db) — Factures                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📞 Points de contact

Pour toute question sur l'architecture ou la maintenance :

- **Architecture** : Microservices avec patterns Event-Driven + Synchronous
- **Communication** : REST + RabbitMQ (asynchrone)
- **Scalabilité** : Chaque service peut être scalé indépendamment
- **Data Isolation** : Base de données dédiée par service (évite couplage)

---

**Fin du rapport**

_Rapport généré : Mai 2026_
