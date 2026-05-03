# 🚀 GUIDE DE DÉPLOIEMENT - Mini-ERP

**Date** : Mai 2026  
**Version** : 1.0.0

---

## 📋 Table des matières

1. [Vue d'ensemble de déploiement](#vue-densemble-de-déploiement)
2. [Mapping des ports](#mapping-des-ports)
3. [Mapping des volumes](#mapping-des-volumes)
4. [Commandes de déploiement](#commandes-de-déploiement)
5. [Vérification du déploiement](#vérification-du-déploiement)
6. [Logs et debugging](#logs-et-debugging)
7. [Escalade du déploiement](#escalade-du-déploiement)

---

## 🎯 Vue d'ensemble de déploiement

Le projet Mini-ERP utilise **Docker Compose** pour orchestrer tous les services.

**Composants déployés** :

- ✅ 1 × Frontend (React + Vite)
- ✅ 1 × API Gateway (Spring Cloud)
- ✅ 3 × Microservices (Stock, Ventes, Finance)
- ✅ 3 × Bases de données PostgreSQL
- ✅ 1 × Message broker RabbitMQ
- ✅ 1 × Réseau Docker Bridge
- ✅ 3 × Volumes nommés pour la persistance

**Durée du déploiement** :

- Premier déploiement : ~5-10 minutes (téléchargement images + build Maven)
- Redémarrages suivants : ~1-2 minutes

---

## 🔌 Mapping des ports

### Ports d'accès externe (Host → Container)

| Service             | Rôle            | Port Host | Port Container | URL                         |
| ------------------- | --------------- | --------- | -------------- | --------------------------- |
| **Frontend**        | Interface React | 3000      | 3000           | http://localhost:3000       |
| **API Gateway**     | Point d'entrée  | 8080      | 8080           | http://localhost:8080       |
| **Stock Service**   | Gestion stock   | 8081      | 8081           | http://localhost:8081       |
| **Ventes Service**  | Gestion ventes  | 8082      | 8082           | http://localhost:8082       |
| **Finance Service** | Gestion finance | 8083      | 8083           | http://localhost:8083       |
| **Stock DB**        | PostgreSQL      | 5433      | 5432           | postgresql://localhost:5433 |
| **Ventes DB**       | PostgreSQL      | 5434      | 5432           | postgresql://localhost:5434 |
| **Finance DB**      | PostgreSQL      | 5435      | 5432           | postgresql://localhost:5435 |
| **RabbitMQ**        | Message broker  | 5672      | 5672           | amqp://localhost:5672       |
| **RabbitMQ Mgmt**   | Management UI   | 15672     | 15672          | http://localhost:15672      |

### Communication interne (Container to Container)

| Service Source  | Service Cible   | Protocole  | Adresse interne                   |
| --------------- | --------------- | ---------- | --------------------------------- |
| Frontend        | API Gateway     | HTTP REST  | http://api-gateway:8080           |
| API Gateway     | Stock Service   | HTTP REST  | http://stock-service:8081         |
| API Gateway     | Ventes Service  | HTTP REST  | http://ventes-service:8082        |
| API Gateway     | Finance Service | HTTP REST  | http://finance-service:8083       |
| Ventes Service  | Stock Service   | HTTP Feign | http://stock-service:8081         |
| Stock Service   | PostgreSQL      | JDBC       | jdbc:postgresql://stock-db:5432   |
| Ventes Service  | PostgreSQL      | JDBC       | jdbc:postgresql://ventes-db:5432  |
| Finance Service | PostgreSQL      | JDBC       | jdbc:postgresql://finance-db:5432 |
| Services        | RabbitMQ        | AMQP       | amqp://rabbitmq:5672              |

---

## 💾 Mapping des volumes

### Volumes nommés Docker

| Nom Volume       | Container Path           | Service    | Description                   |
| ---------------- | ------------------------ | ---------- | ----------------------------- |
| **stock-data**   | /var/lib/postgresql/data | stock-db   | Données PostgreSQL Stock DB   |
| **ventes-data**  | /var/lib/postgresql/data | ventes-db  | Données PostgreSQL Ventes DB  |
| **finance-data** | /var/lib/postgresql/data | finance-db | Données PostgreSQL Finance DB |

### Volumes bind (Développement)

| Host Path         | Container Path | Service         | Description                       |
| ----------------- | -------------- | --------------- | --------------------------------- |
| ./frontend        | /app           | erp-frontend    | Code source frontend (hot reload) |
| ./api-gateway     | /app           | api-gateway     | Code source (build seulement)     |
| ./stock-service   | /app           | stock-service   | Code source (build seulement)     |
| ./ventes-service  | /app           | ventes-service  | Code source (build seulement)     |
| ./finance-service | /app           | finance-service | Code source (build seulement)     |

### Inspectez les volumes

```bash
# Lister tous les volumes
docker volume ls

# Inspecter un volume
docker volume inspect mini_erp_stock-data

# Afficher où est stocké le volume physiquement
docker volume inspect mini_erp_stock-data | grep Mountpoint
```

---

## 🚀 Commandes de déploiement

### Option 1 : Déploiement complet avec Docker Compose (Recommandé)

```bash
# Aller au répertoire racine
cd /c/Users/ADMIN/mini_erp

# Démarrer tous les services (build + run)
docker compose up --build

# Arrière-plan (mode détaché)
docker compose up -d --build

# Arrêter les services
docker compose down

# Arrêter et supprimer les volumes (ATTENTION: perte de données)
docker compose down -v
```

### Option 2 : Déploiement sélectif

```bash
# Démarrer uniquement les bases de données et RabbitMQ
docker compose up --build stock-db ventes-db finance-db rabbitmq

# Dans un autre terminal : démarrer les services
docker compose up --build api-gateway stock-service ventes-service finance-service

# Dans un 3ème terminal : démarrer le frontend local
cd frontend
npm install
npm run dev
```

### Option 3 : Frontend local + Backend Docker

```bash
# Terminal 1 : Backend avec Docker
docker compose up --build

# Terminal 2 : Frontend local
cd frontend
npm install
npm run dev

# Frontend: http://localhost:3000
# Backend API: http://localhost:8080
```

### Rebuild d'un service spécifique

```bash
# Rebuild du Stock Service
docker compose up -d --build stock-service

# Rebuild du Ventes Service
docker compose up -d --build ventes-service

# Rebuild de Finance Service
docker compose up -d --build finance-service

# Rebuild du Gateway
docker compose up -d --build api-gateway
```

---

## ✅ Vérification du déploiement

### 1. Vérifier l'état des conteneurs

```bash
# Afficher tous les conteneurs
docker ps

# Vérifier l'état : tous doivent être "Up"
# Expected output:
# CONTAINER ID   IMAGE                              STATUS            PORTS
# xxxxxxxx       mini_erp-stock-service            Up 2 minutes      0.0.0.0:8081->8081/tcp
# xxxxxxxx       mini_erp-ventes-service           Up 2 minutes      0.0.0.0:8082->8082/tcp
# xxxxxxxx       mini_erp-finance-service          Up 2 minutes      0.0.0.0:8083->8083/tcp
# xxxxxxxx       mini_erp-api-gateway              Up 2 minutes      0.0.0.0:8080->8080/tcp
# xxxxxxxx       mini_erp-erp-frontend            Up 2 minutes      0.0.0.0:3000->3000/tcp
# xxxxxxxx       rabbitmq:3.13-management-alpine  Up 2 minutes      0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
# xxxxxxxx       postgres:16-alpine                Up 2 minutes      0.0.0.0:5433->5432/tcp
# xxxxxxxx       postgres:16-alpine                Up 2 minutes      0.0.0.0:5434->5432/tcp
# xxxxxxxx       postgres:16-alpine                Up 2 minutes      0.0.0.0:5435->5432/tcp
```

### 2. Tester la connectivité

```bash
# Test du Frontend
curl -I http://localhost:3000

# Test de l'API Gateway
curl -I http://localhost:8080

# Test Stock Service (via Gateway)
curl http://localhost:8080/api/stock/produits

# Test Ventes Service (via Gateway)
curl http://localhost:8080/api/ventes/commandes

# Test Finance Service (via Gateway)
curl http://localhost:8080/api/finance/factures

# Test RabbitMQ Management UI
curl -I http://localhost:15672
```

### 3. Test complet d'une requête

```bash
# Créer un produit
curl -X POST http://localhost:8080/api/stock/produits \
  -H "Content-Type: application/json" \
  -d '{
    "reference": "TEST-001",
    "nom": "Produit Test",
    "prixUnitaire": 100.00,
    "quantiteEnStock": 50,
    "seuilAlerte": 5
  }'

# Affiche : {"id": 1, "reference": "TEST-001", ...}
```

### 4. Vérifier les bases de données

```bash
# Connexion à Stock DB
psql -h localhost -p 5433 -U erp -d stock_db

# Lister les tables
\dt

# Requête test
SELECT * FROM produit;

# Quitter
\q
```

### 5. Vérifier RabbitMQ

**Management UI** : http://localhost:15672

- Utilisateur : `erp`
- Mot de passe : `erp_secret`

**Via CLI** :

```bash
# Lister les exchanges
docker exec rabbitmq rabbitmqctl list_exchanges

# Lister les queues
docker exec rabbitmq rabbitmqctl list_queues

# Lister les bindings
docker exec rabbitmq rabbitmqctl list_bindings
```

---

## 📊 Logs et debugging

### Afficher les logs en temps réel

```bash
# Tous les services
docker compose logs -f

# Un service spécifique
docker compose logs -f stock-service
docker compose logs -f ventes-service
docker compose logs -f finance-service
docker compose logs -f api-gateway
docker compose logs -f erp-frontend
docker compose logs -f rabbitmq

# Dernières 50 lignes
docker compose logs --tail=50 stock-service

# Logs depuis X minutes
docker compose logs --since 10m stock-service
```

### Accéder à un conteneur

```bash
# Bash dans le conteneur Stock Service
docker compose exec stock-service bash

# Shell dans PostgreSQL
docker compose exec stock-db psql -U erp -d stock_db

# Shell dans RabbitMQ
docker compose exec rabbitmq sh
```

### Redémarrer un service spécifique

```bash
# Restart avec logs
docker compose restart stock-service
docker compose logs -f stock-service

# Ou redémarrer et rebuild
docker compose up -d --build stock-service
```

### Nettoyer les ressources Docker

```bash
# Supprimer tous les conteneurs arrêtés
docker container prune

# Supprimer toutes les images inutilisées
docker image prune

# Supprimer tous les volumes inutilisés
docker volume prune

# Nettoyage complet (ATTENTION: destructeur!)
docker compose down -v
docker system prune -a
```

---

## 📈 Escalade du déploiement

### Scaler un service (augmenter les répliques)

```bash
# Augmenter Stock Service à 3 répliques
docker compose up -d --scale stock-service=3

# Vérifier
docker ps | grep stock-service
# Affiche: 3 instances
```

### Ajouter un load balancer (nginx)

Créer `docker-compose.override.yml` :

```yaml
version: "3.8"
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - api-gateway
    networks:
      - erp-net
```

### Déployer sur un serveur distant

```bash
# Sur le serveur distant
scp -r /c/Users/ADMIN/mini_erp user@remote:/home/user/

# SSH et démarrer
ssh user@remote
cd /home/user/mini_erp
docker compose up -d --build

# Vérifier depuis votre machine
curl http://remote_server:8080/api/stock/produits
```

### Monitoring avec Prometheus et Grafana

Ajouter les conteneurs au `docker-compose.yml` :

```yaml
prometheus:
  image: prom/prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3001:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
```

---

## 🔐 Sécurité en production

### Points à configurer avant production

```yaml
# docker-compose.prod.yml
services:
  api-gateway:
    environment:
      # Activer HTTPS
      SERVER_SSL_ENABLED: "true"
      SERVER_SSL_KEY_STORE: /etc/ssl/keystore.jks
      # Désactiver Actuator
      MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: ""

  rabbitmq:
    environment:
      # Changer les credentials par défaut
      RABBITMQ_DEFAULT_USER: ${RABBITMQ_USER}
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASS}
      # Activer les plugins SSL
      RABBITMQ_SSL_CERTFILE: /etc/rabbitmq/certs/cert.pem
      RABBITMQ_SSL_KEYFILE: /etc/rabbitmq/certs/key.pem

  stock-db:
    environment:
      # Utiliser .env pour les secrets
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_USER: ${POSTGRES_USER}
```

### Fichier `.env`

```env
# Credentials
POSTGRES_USER=erp_prod
POSTGRES_PASSWORD=SuperSecurePassword123!
RABBITMQ_USER=rabbitmq_prod
RABBITMQ_PASS=RabbitSecure456!

# Domaine
API_DOMAIN=api.example.com
FRONTEND_URL=https://erp.example.com

# Features
ENABLE_HTTPS=true
LOG_LEVEL=INFO
```

---

## 📱 Dashboard et monitoring

### Accès aux interfaces

| Interface           | URL                    | Credentials      |
| ------------------- | ---------------------- | ---------------- |
| Frontend            | http://localhost:3000  | -                |
| API Gateway         | http://localhost:8080  | -                |
| RabbitMQ UI         | http://localhost:15672 | erp / erp_secret |
| pgAdmin (optionnel) | http://localhost:5050  | -                |

### Configurer pgAdmin (Admin PostgreSQL)

```yaml
pgadmin:
  image: dpage/pgadmin4
  ports:
    - "5050:80"
  environment:
    PGADMIN_DEFAULT_EMAIL: admin@erp.local
    PGADMIN_DEFAULT_PASSWORD: admin
  networks:
    - erp-net
```

---

## 🎯 Checklist de déploiement

- [ ] Docker et Docker Compose installés
- [ ] Dépendances npm installées (`npm install` dans `./frontend`)
- [ ] Ports 3000, 5672, 5433-5435, 8080-8083, 15672 disponibles
- [ ] Au moins 4GB de RAM disponible
- [ ] 10GB d'espace disque disponible
- [ ] Tous les fichiers `.env` configurés
- [ ] `.git` ou `.gitignore` pour les secrets
- [ ] Volumes Docker préparés
- [ ] Test de connectivité vers les services
- [ ] Logs vérifiés (pas d'erreurs critiques)
- [ ] Base de données initialisées avec des données de test
- [ ] Frontend accessible et communique avec le backend
- [ ] Flux complet testé (créer produit → commande → paiement)

---

## 🆘 Troubleshooting

| Problème                                        | Solution                                                                            |
| ----------------------------------------------- | ----------------------------------------------------------------------------------- |
| **Port 8080 déjà utilisé**                      | `lsof -i :8080` puis tuer le processus ou changer le port dans `docker-compose.yml` |
| **Conteneur s'arrête immédiatement**            | Vérifier les logs: `docker compose logs stock-service`                              |
| **Connexion DB refusée**                        | Vérifier les credentials, attendre que DB soit prête (~10s)                         |
| **RabbitMQ ne démarre pas**                     | Vérifier l'espace disque, augmenter la RAM allouée à Docker                         |
| **Frontend ne se connecte pas au backend**      | Vérifier CORS config, tester `curl http://localhost:8080`                           |
| **Données perdues après `docker compose down`** | Utilisé `down -v` ? Cela supprime les volumes. Ne pas utiliser `-v` pour production |
| **Service Java OUT OF MEMORY**                  | Augmenter la limite JVM: `JAVA_OPTS: "-Xmx512m"`                                    |

---

**Fin du guide de déploiement**

_Dernière mise à jour : Mai 2026_
