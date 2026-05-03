# 📡 Guide Java RMI - Mini-ERP

**Date** : Mai 2026  
**Version** : 1.0.0  
**Sujet** : Intégration Java RMI pour la communication inter-services

---

## 📑 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture RMI](#architecture-rmi)
3. [Configuration](#configuration)
4. [Utilisation](#utilisation)
5. [Exemples](#exemples)
6. [Dépannage](#dépannage)
7. [Sécurité RMI](#sécurité-rmi)

---

## 🎯 Vue d'ensemble

Java RMI (Remote Method Invocation) a été intégré au projet Mini-ERP pour permettre une communication **synchrone et fortement typée** entre les services microservices.

### Caractéristiques

- ✅ **Communication synchrone** : Appels RMI directs entre services
- ✅ **Typage fort** : Interfaces RMI avec types Java
- ✅ **Gestion d'erreurs** : RemoteException pour les erreurs de communication
- ✅ **Performance** : Appels directs sans sérialisation JSON
- ✅ **Complément REST** : Coexiste avec REST/HTTP existant
- ✅ **Complément RabbitMQ** : Coexiste avec la messagerie asynchrone

### Quand utiliser RMI ?

| Cas d'usage                           | REST | RabbitMQ | RMI |
| ------------------------------------- | ---- | -------- | --- |
| Communication synchrone simple        | ✅   | ❌       | ✅  |
| Appels inter-services fortement typés | ⚠️   | ❌       | ✅  |
| Communication asynchrone              | ❌   | ✅       | ❌  |
| Notifications à plusieurs services    | ❌   | ✅       | ❌  |
| Haute performance (LAN)               | ⚠️   | ⚠️       | ✅  |

---

## 🏗️ Architecture RMI

### Structure du projet RMI

```
rmi-common/                           # Module RMI commun
├── pom.xml
├── src/main/java/com/erp/rmi/
│   ├── StockServiceRmi.java          # Interface RMI
│   ├── VentesServiceRmi.java         # Interface RMI
│   └── FinanceServiceRmi.java        # Interface RMI

stock-service/                         # Service Stock
├── src/main/java/com/erp/stock/
│   ├── rmi/
│   │   └── StockServiceRmiImpl.java   # Implémentation RMI
│   └── config/
│       ├── RmiStockConfiguration.java # Exportation RMI
│       └── RmiStockClientConfiguration.java # Proxies clients

ventes-service/                        # Service Ventes
├── src/main/java/com/erp/ventes/
│   ├── rmi/
│   │   └── VentesServiceRmiImpl.java
│   └── config/
│       ├── RmiVentesConfiguration.java
│       └── RmiVentesClientConfiguration.java

finance-service/                       # Service Finance
├── src/main/java/com/erp/finance/
│   ├── rmi/
│   │   └── FinanceServiceRmiImpl.java
│   └── config/
│       ├── RmiFinanceConfiguration.java
│       └── RmiFinanceClientConfiguration.java
```

### Ports RMI

| Service | Port RMI | URL RMI                                     |
| ------- | -------- | ------------------------------------------- |
| Stock   | 1099     | `rmi://stock-service:1099/StockService`     |
| Ventes  | 1100     | `rmi://ventes-service:1100/VentesService`   |
| Finance | 1101     | `rmi://finance-service:1101/FinanceService` |

---

## ⚙️ Configuration

### 1. Dépendances Maven

Chaque service inclut les dépendances suivantes :

```xml
<!-- Module RMI commun -->
<dependency>
    <groupId>com.erp</groupId>
    <artifactId>rmi-common</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Spring Remoting pour RMI -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context-support</artifactId>
</dependency>
```

### 2. Configuration Docker Compose

Chaque service expose son port RMI :

```yaml
stock-service:
  ports:
    - "8081:8081" # Application
    - "1099:1099" # RMI Registry

ventes-service:
  ports:
    - "8082:8082" # Application
    - "1100:1100" # RMI Registry

finance-service:
  ports:
    - "8083:8083" # Application
    - "1101:1101" # RMI Registry

environment:
  JAVA_TOOL_OPTIONS: "-Djava.rmi.server.hostname=<service-name>"
  RMI_ENABLED: "true"
```

### 3. Configuration application.yml

Chaque service active RMI via :

```yaml
rmi:
  enabled: ${RMI_ENABLED:true}
```

---

## 🚀 Utilisation

### 1. Injection des proxies RMI

Les services injectent automatiquement les proxies RMI via Spring :

```java
@Service
public class MyService {

    private final StockServiceRmi stockServiceRmi;
    private final VentesServiceRmi ventesServiceRmi;

    @Autowired(required = false)
    public MyService(StockServiceRmi stockServiceRmi,
                     VentesServiceRmi ventesServiceRmi) {
        this.stockServiceRmi = stockServiceRmi;
        this.ventesServiceRmi = ventesServiceRmi;
    }
}
```

### 2. Appels RMI

Effectuez des appels RMI comme des appels locaux :

```java
try {
    // Appel RMI synchrone
    Boolean available = stockServiceRmi.isProductAvailable(productId, quantity);

    if (available) {
        // Réserver le produit
        Boolean reserved = stockServiceRmi.reserveProduct(productId, quantity);
    }
} catch (RemoteException e) {
    // Gérer les erreurs de communication
    log.error("Erreur RMI", e);
}
```

### 3. Gestion des erreurs

Toujours entourer les appels RMI dans des blocs try-catch :

```java
try {
    result = remoteService.someMethod();
} catch (RemoteException e) {
    // Erreur de communication RMI
    log.error("Service RMI indisponible", e);
    // Implémenter un fallback ou retry
}
```

---

## 💡 Exemples

### Exemple 1 : Vérifier la disponibilité d'un produit (Ventes Service)

```java
@Service
public class OrderService {

    private final StockServiceRmi stockServiceRmi;

    public void createOrder(OrderRequest request) {
        try {
            // Vérifier la disponibilité via RMI
            Boolean available = stockServiceRmi.isProductAvailable(
                request.getProductId(),
                request.getQuantity()
            );

            if (available) {
                // Réserver le produit
                stockServiceRmi.reserveProduct(
                    request.getProductId(),
                    request.getQuantity()
                );

                // Créer la commande
                Order order = new Order();
                order.setProductId(request.getProductId());
                order.setQuantity(request.getQuantity());
                orderRepository.save(order);
            }
        } catch (RemoteException e) {
            log.error("Impossible de vérifier la disponibilité du stock", e);
            throw new OrderException("Service Stock indisponible");
        }
    }
}
```

### Exemple 2 : Créer une facture (Finance Service)

```java
@Service
public class InvoiceService {

    private final VentesServiceRmi ventesServiceRmi;

    public void createInvoiceForOrder(Long orderId) {
        try {
            // Récupérer les infos de la commande via RMI
            Double orderTotal = ventesServiceRmi.getOrderTotal(orderId);
            String orderStatus = ventesServiceRmi.getOrderStatus(orderId);

            // Créer la facture
            Invoice invoice = new Invoice();
            invoice.setOrderId(orderId);
            invoice.setAmount(orderTotal);
            invoice.setStatus("DRAFT");

            invoiceRepository.save(invoice);

        } catch (RemoteException e) {
            log.error("Impossible de créer la facture, service Ventes indisponible", e);
            throw new InvoiceException("Service Ventes indisponible");
        }
    }
}
```

### Exemple 3 : Service RMI Communication (Pattern recommandé)

```java
@Service
public class RmiCommunicationService {

    private final StockServiceRmi stockServiceRmi;

    @Autowired(required = false)
    public RmiCommunicationService(StockServiceRmi stockServiceRmi) {
        this.stockServiceRmi = stockServiceRmi;
    }

    /**
     * Récupère la quantité d'un produit via RMI
     * Avec gestion d'erreurs et logging
     */
    public Integer getProductQuantity(Long productId) {
        try {
            if (stockServiceRmi != null) {
                log.debug("Appel RMI: getProductQuantity({})", productId);
                return stockServiceRmi.getProductQuantity(productId);
            } else {
                log.warn("StockServiceRmi non disponible");
                return 0;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI", e);
            return 0;
        }
    }
}
```

---

## 🔧 Dépannage

### Problème 1 : "Connection refused" ou "Service not found"

```
java.rmi.NotBoundException: StockService
java.rmi.ConnectException: Connection refused to host: stock-service
```

**Causes** :

- Le service RMI n'a pas démarré
- Le port RMI n'est pas accessible
- Le port n'est pas exposé dans Docker Compose

**Solutions** :

1. Vérifier que le service est démarré : `docker ps`
2. Vérifier les logs du service : `docker logs stock-service`
3. Vérifier la configuration RMI dans application.yml
4. Vérifier les ports dans docker-compose.yml

```bash
# Tester la connexion au port RMI
telnet stock-service 1099
```

### Problème 2 : "java.rmi.server.hostname" non défini

```
java.rmi.server.ExportException: Port already in use
```

**Solution** :
Ajouter la variable d'environnement JAVA_TOOL_OPTIONS dans docker-compose.yml :

```yaml
environment:
  JAVA_TOOL_OPTIONS: "-Djava.rmi.server.hostname=stock-service"
```

### Problème 3 : Proxy RMI non injecté (null)

**Cause** : L'interface RMI n'a pas pu se connecter au service distant.

**Solution** :

```java
@Autowired(required = false)  // Important !
public MyService(StockServiceRmi stockServiceRmi) {
    this.stockServiceRmi = stockServiceRmi;
}

// Dans la méthode, vérifier si le proxy est disponible
if (stockServiceRmi != null) {
    // Utiliser le proxy
}
```

---

## 🔐 Sécurité RMI

### 1. Restreindre l'accès RMI

Par défaut, RMI écoute sur tous les interfaces. Pour restreindre :

```java
@Bean
public RmiServiceExporter stockServiceRmiExporter() {
    RmiServiceExporter exporter = new RmiServiceExporter();
    exporter.setRegistryHost("localhost");  // Seulement localhost
    exporter.setRegistryPort(1099);
    return exporter;
}
```

### 2. Utiliser des pare-feu

Dans Docker Compose, les services peuvent communiquer sur le réseau erp-net mais ne pas exposer les ports RMI à l'extérieur :

```yaml
# Ne pas exposer le port RMI à l'hôte
# stock-service:
#   ports:
#     - "1099:1099"  # À éviter en production
```

### 3. Authentification SSL/TLS

Pour une production sécurisée, implémenter SSL pour RMI :

```bash
# Générer les certificats
keytool -genkey -alias rmi-server -keystore rmi-keystore.jks

# Configurer dans application.yml
system:
  properties:
    javax.net.ssl.keyStore: /path/to/rmi-keystore.jks
    javax.net.ssl.keyStorePassword: password
```

---

## 📊 Comparaison : REST vs RabbitMQ vs RMI

| Aspect                    | REST        | RabbitMQ      | RMI          |
| ------------------------- | ----------- | ------------- | ------------ |
| **Type de communication** | Synchrone   | Asynchrone    | Synchrone    |
| **Couplage**              | Lâche       | Très lâche    | Fort         |
| **Performance**           | Modérée     | Haute (async) | Haute (sync) |
| **Typage**                | Weak (JSON) | None          | Fort         |
| **Latence**               | Élevée      | N/A           | Basse        |
| **Scalabilité**           | Bonne       | Excellente    | Modérée      |
| **Fiabilité**             | Modérée     | Excellente    | Modérée      |
| **Complexité**            | Faible      | Modérée       | Modérée      |

---

## 🚀 Démarrer avec RMI

### 1. Construire le projet

```bash
# Construire le module RMI commun
cd rmi-common
mvn clean install

# Revenir à la racine
cd ..
```

### 2. Lancer les services avec Docker Compose

```bash
docker-compose up -d
```

### 3. Vérifier les ports RMI

```bash
# Vérifier que les services écoutent sur les ports RMI
netstat -an | grep -E "1099|1100|1101"

# Ou dans Docker
docker exec stock-service netstat -an | grep 1099
```

### 4. Tester les appels RMI

Regarder les logs des services :

```bash
docker logs -f stock-service
docker logs -f ventes-service
docker logs -f finance-service
```

Vous devriez voir :

```
INFO  [...] Configuration de l'exportation RMI pour le service Stock
INFO  [...] Service Stock RMI exporté sur rmi://localhost:1099/StockService
```

---

## 📚 Ressources

- [Oracle RMI Documentation](https://docs.oracle.com/javase/tutorial/rmi/)
- [Spring Remoting Documentation](https://spring.io/projects/spring-framework)
- [Java RMI Best Practices](https://www.oracle.com/technical-resources/articles/java/rmi.html)

---

## ✅ Checklist d'intégration RMI

- [ ] Module `rmi-common` créé et construit
- [ ] Dépendances `spring-context-support` ajoutées aux services
- [ ] Interfaces RMI implémentées
- [ ] Configuration RMI (exportation) ajoutée aux services
- [ ] Proxies RMI clients configurés
- [ ] Ports RMI exposés dans docker-compose.yml
- [ ] Variable d'environnement `JAVA_TOOL_OPTIONS` configurée
- [ ] Tests de communication RMI effectués
- [ ] Logging et monitoring en place
- [ ] Gestion d'erreurs RMI implémentée

---

**Dernière mise à jour** : Mai 2026  
**Mainteneur** : Équipe ERP  
**Status** : ✅ Production Ready
