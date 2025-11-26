# 🚀 Système de Gestion de Recrutement

## 📋 Description

Application web complète de gestion de recrutement développée avec **Spring Boot 3.5.8** (backend) et **Angular 20** (frontend). Le système permet de gérer les offres d'emploi, les candidatures, et d'automatiser le processus de recrutement avec analyse IA et notifications en temps réel.

## 🏗️ Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.5.8
- **Base de données**: MariaDB
- **Sécurité**: JWT Authentication
- **Email**: Gmail SMTP
- **API**: REST avec documentation Swagger

### Frontend (Angular)
- **Framework**: Angular 20
- **UI**: Bootstrap 5 + Template personnalisé
- **Architecture**: Composants standalone
- **Routing**: Guards d'authentification
- **Notifications**: Système temps réel

## 🎯 Fonctionnalités Principales

### 📊 Dashboard
- Statistiques en temps réel (candidatures, validations, rejets)
- Cartes colorées avec métriques clés
- Tableau des candidatures récentes
- Notifications d'applications non lues

### 💼 Gestion des Offres d'Emploi
- Création/modification d'offres
- Statut actif/inactif
- Détails complets avec candidatures associées
- Bouton de rafraîchissement dynamique

### 👥 Gestion des Candidatures
- **Statuts**: PENDING, VALIDATED, AMBIGUOUS, REJECTED
- **Suivi de lecture**: Système viewed/unviewed avec timestamps
- **Score IA**: Analyse automatique des profils (0-20)
- **Actions en lot**: Validation/rejet multiple avec emails
- **Actions rapides**: Modals Bootstrap pour traitement individuel
- **Archivage automatique**: Candidatures rejetées archivées
- **Vue séparée**: Candidatures archivées accessibles

### 🔍 Système de Filtrage
- Filtres par statut (Toutes, En attente, Validées, Ambiguës, Rejetées)
- Recherche en temps réel
- Compteurs dynamiques par filtre

### ✅ Workflow de Validation
- **Obligation de lecture**: Candidatures doivent être vues avant validation/rejet
- **Candidats validés**: Table séparée dans les détails d'offre
- **Restrictions**: Actions bloquées si non lue

### 📧 Système d'Email
- **Configuration SMTP**: Gmail intégré
- **Templates personnalisés**: Emails de validation/rejet
- **Placeholder dynamique**: [nom du candidat] remplacé automatiquement
- **Envoi automatique**: Lors des actions en lot

### 🔔 Notifications Temps Réel
- **Badge de compteur**: Applications non lues
- **Dropdown interactif**: Liste des notifications avec liens
- **Marquage automatique**: Lecture lors du clic
- **Groupement par offre**: Notifications organisées

### 🔐 Authentification & Sécurité
- **JWT Tokens**: Authentification sécurisée
- **Route Guards**: Protection des pages
- **Redirections automatiques**: Login/logout
- **Session management**: Gestion des tokens

## 🗂️ Structure du Projet

```
Spring Boot Template/
├── backend/                    # Spring Boot Application
│   ├── src/main/java/
│   │   ├── controller/        # REST Controllers
│   │   ├── service/          # Business Logic
│   │   ├── repository/       # Data Access Layer
│   │   ├── entity/           # JPA Entities
│   │   └── config/           # Configuration
│   └── src/main/resources/
│       ├── application.properties
│       └── data.sql
├── angular/                   # Angular Frontend
│   ├── src/app/
│   │   ├── demo/dashboard/   # Dashboard Components
│   │   ├── services/         # Angular Services
│   │   ├── theme/layout/     # Layout Components
│   │   └── guards/           # Route Guards
│   └── src/assets/           # Static Assets
└── README.md
```

## 🚀 Installation & Configuration

### Prérequis
- **Java 17+**
- **Node.js 18+**
- **MariaDB 10.6+**
- **Maven 3.8+**
- **Angular CLI 20+**

### Backend Setup

1. **Configuration Base de Données**
```properties
# application.properties
spring.datasource.url=jdbc:mariadb://localhost:3306/recruitment_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

2. **Configuration Email**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

3. **Configuration JWT**
```properties
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000
```

4. **Démarrage**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend Setup

1. **Installation des dépendances**
```bash
cd angular
npm install
```

2. **Configuration API**
```typescript
// src/app/services/application.service.ts
private apiUrl = 'http://localhost:8080/api';
```

3. **Démarrage**
```bash
ng serve
```

## 🔧 Configuration Avancée

### Base de Données
```sql
-- Création de la base
CREATE DATABASE recruitment_db;

-- Tables principales
- applications (candidatures)
- candidates (candidats)  
- job_offers (offres d'emploi)
- users (utilisateurs)
```

### Variables d'Environnement
```bash
# Backend
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:mariadb://localhost:3306/recruitment_db
JWT_SECRET=your_secret_key
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Frontend
NG_APP_API_URL=http://localhost:8080/api
```

## 📱 Utilisation

### Connexion
1. Accéder à `http://localhost:4200`
2. Se connecter avec les identifiants
3. Redirection automatique vers le dashboard

### Workflow Type
1. **Consulter le dashboard** - Vue d'ensemble des métriques
2. **Gérer les offres** - Créer/modifier les postes
3. **Traiter les candidatures** - Lire, analyser, décider
4. **Actions en lot** - Valider/rejeter plusieurs candidatures
5. **Suivi des notifications** - Nouvelles applications en temps réel

## 🔍 API Endpoints

### Authentification
- `POST /api/auth/login` - Connexion
- `POST /api/auth/logout` - Déconnexion

### Dashboard
- `GET /api/dashboard/stats` - Statistiques générales
- `GET /api/dashboard/recent-applications` - Candidatures récentes

### Candidatures
- `GET /api/applications` - Liste complète
- `GET /api/applications/status/{status}` - Par statut
- `PUT /api/applications/{id}/status` - Changer statut
- `POST /api/applications/bulk-action` - Actions en lot
- `GET /api/applications/archived` - Candidatures archivées

### Notifications
- `GET /api/applications/notifications/unread` - Non lues
- `POST /api/applications/notifications/mark-read/{jobOfferId}` - Marquer comme lues

## 🎨 Interface Utilisateur

### Composants Principaux
- **Dashboard Cards**: Métriques colorées (bleu, vert, jaune, rouge)
- **Data Tables**: Tableaux interactifs avec filtres
- **Bootstrap Modals**: Actions rapides et confirmations
- **Notification Dropdown**: Badge avec compteur et liste
- **Status Badges**: Indicateurs visuels des statuts

### Responsive Design
- **Mobile First**: Interface adaptative
- **Bootstrap Grid**: Layout responsive
- **Touch Friendly**: Interactions tactiles optimisées

## 🔒 Sécurité

### Mesures Implémentées
- **JWT Authentication**: Tokens sécurisés
- **Route Guards**: Protection des pages
- **CORS Configuration**: Contrôle d'accès
- **Input Validation**: Validation côté client/serveur

### Points d'Attention
⚠️ **Secrets JWT**: Utiliser des clés fortes en production
⚠️ **HTTPS**: Obligatoire en production
⚠️ **Variables d'environnement**: Ne pas exposer les credentials

## 🐛 Dépannage

### Problèmes Courants

**Backend ne démarre pas**
- Vérifier la connexion MariaDB
- Contrôler les credentials dans application.properties

**Frontend erreurs CORS**
- Vérifier la configuration CORS dans Spring Boot
- S'assurer que l'URL API est correcte

**Emails non envoyés**
- Vérifier les paramètres SMTP Gmail
- Utiliser un mot de passe d'application Google

**Notifications non mises à jour**
- Vérifier les endpoints de notification
- Contrôler les requêtes réseau dans DevTools

## 📈 Performance

### Optimisations Implémentées
- **Lazy Loading**: Chargement à la demande
- **Pagination**: Tables avec pagination
- **Caching**: Mise en cache des données statiques
- **Compression**: Assets compressés

### Métriques
- **Temps de chargement**: < 2s
- **Bundle size**: Optimisé avec tree-shaking
- **Database queries**: Optimisées avec JPA

## 🚀 Déploiement

### Production
```bash
# Backend
mvn clean package -Pprod
java -jar target/recruitment-app.jar

# Frontend  
ng build --prod
# Déployer dist/ sur serveur web
```

### Docker (Optionnel)
```dockerfile
# Dockerfile backend
FROM openjdk:17-jre-slim
COPY target/recruitment-app.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

## 👥 Équipe & Contributions

### Développement
- **Architecture**: Spring Boot + Angular
- **Base de données**: MariaDB avec JPA/Hibernate
- **Frontend**: Angular 20 avec Bootstrap 5
- **Authentification**: JWT avec Guards

### Fonctionnalités Clés Développées
- ✅ Système de candidatures avec workflow complet
- ✅ Dashboard avec métriques temps réel
- ✅ Notifications interactives
- ✅ Actions en lot avec emails automatiques
- ✅ Archivage automatique des rejets
- ✅ Interface responsive et moderne

## 📞 Support

Pour toute question ou problème :
1. Consulter cette documentation
2. Vérifier les logs backend/frontend
3. Contrôler la configuration de la base de données
4. Tester les endpoints API avec Postman

---

**Version**: 1.0.0  
**Dernière mise à jour**: Novembre 2024  
**Technologies**: Spring Boot 3.5.8, Angular 20, MariaDB, Bootstrap 5