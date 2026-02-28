# Application Full-Stack MDD-API

Projet OpenClassrooms : API et Frontend pour un réseau social de développeurs.

## Structure du projet

- `back/` : API Java Spring Boot (src/main/java, src/main/resources, tests, pom.xml)
- `front/` : Application Angular (src/, tests, configuration, package.json)
- `specs/` : Spécifications fonctionnelles et techniques (PDF/Markdown)
- `postman/` : Collection Postman pour tester l’API
- Fichiers de documentation : `agent.md`, `rules.md`, `historique.md`, `historique.git.md`

## Architecture globale de l’application

Ce schéma donne une vue d’ensemble des interactions entre l’utilisateur, le frontend Angular, le backend Spring Boot et la base de données :

```mermaid
flowchart TD
  subgraph Utilisateur
    U[Utilisateur Web]
  end
  subgraph Frontend [Angular]
    F1[App Angular]
    F2[AuthGuard, Interceptors]
    F3[Services/API]
    F4[Environnements]
  end
  subgraph Backend [Spring Boot]
    B1[API REST]
    B2[Contrôleurs]
    B3[Services Métier]
    B4[Repositories]
    B5[Entities/JPA]
    B6[Sécurité JWT/CSRF]
  end
  subgraph DB [MySQL]
    DB[(Base de données)]
  end
  U-->|HTTP(S)|F1
  F1-->|Appels API|F3
  F3-->|/api/*|B1
  B1-->|Appelle|B2
  B2-->|Appelle|B3
  B3-->|Appelle|B4
  B4-->|Lit/écrit|B5
  B4-->|SQL|DB
  B1-->|Sécurité|B6
  F1-->|Gestion session|F2
  F2-->|Token/Cookie|B6
  F1-->|Assets statiques|F4
```

**Méthode d’intégration :**
- Ce bloc Mermaid peut être copié tel quel dans un README, un fichier Markdown ou un outil compatible (VS Code, GitHub, HackMD…).
- Si tu veux une image (SVG/PNG), demande-le et je peux te la générer.

## Prérequis

- Java 21+
- Node.js 18+
- MySQL 8+

## Installation et configuration

1. **Base de données** :
   - **Définissez d'abord les variables d’environnement** à utiliser dans le script SQL et dans la configuration Spring Boot :
     - `DB_USER` : le nom d'utilisateur MySQL (ex : kevin)
     - `DB_PASSWORD` : le mot de passe MySQL choisi (ex : votre_mot_de_passe)
     - `DB_MDD_NAME` : le nom de la base de données (ex : mdd_db)
     - Sous Windows :
       - Session courante : `set DB_USER="kevin"` etc.
       - Persistant : `setx DB_USER "kevin"` etc.
   - **Important :** Ces variables doivent être accessibles à Spring Boot (soit dans l'environnement système, soit dans `src/main/resources/application.properties` via `${DB_USER}` etc.).
   - **Adaptez le script** `back/src/main/resources/base.sql` en remplaçant les valeurs par vos variables/envies :
     ```sql
     CREATE DATABASE mdd_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     CREATE USER 'kevin'@'localhost' IDENTIFIED BY 'votre_mot_de_passe';
     GRANT SELECT, INSERT, UPDATE, DELETE, ALTER, CREATE, DROP, REFERENCES ON mdd_db.* TO 'kevin'@'localhost';
     FLUSH PRIVILEGES;
     ```
   - **Exécutez ensuite ce script** dans MySQL pour créer la base et l’utilisateur avec les droits nécessaires.
2. **Backend** :
   - Placez-vous dans `back/`.
   - Lancez `./mvnw.cmd clean package` (Windows) ou `./mvnw clean package` (Linux/Mac).
   - Fichier de config : `src/main/resources/application.properties`.

3. **Frontend** :
   - Placez-vous dans `front/`.
   - Lancez `npm install` puis `npm run start` (ou `npm run start:e2e` pour le port 4201 avec proxy).

## Gestion de l'environnement Spring Boot

Le profil actif de l'application backend est défini par la propriété `spring.profiles.active`.

- Par défaut, la valeur est `dev` (voir `back/src/main/resources/application.properties`).
- Vous pouvez la surcharger via une variable d'environnement, un argument JVM ou un autre fichier de configuration.
- Exemple : pour lancer en mode production : `SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run`

Si aucune valeur n'est définie, le profil `dev` sera utilisé par défaut.

## Lancement

- **Backend** :
	- `cd back`
	- `./mvnw.cmd spring-boot:run` (Windows) ou `./mvnw spring-boot:run`
	- L’API écoute sur `http://localhost:8080/api`
- **Frontend** :
	- `cd front`
	- `npm run start` (http://localhost:4200) ou `npm run start:e2e` (http://localhost:4201)

## Tester le front Angular minifié en local (production) avec Lighthouse

> **Optionnel :** Cette étape est recommandée pour l’audit de performance et d’accessibilité, mais n’est pas obligatoire pour le fonctionnement de l’application.

### Prérequis (à faire une seule fois)

1. **Installer les dépendances nécessaires pour le serveur Express :**
   - Placez-vous dans `front/`.
   - Installez :
     ```
     npm install express http-proxy-middleware
     ```
   - Vérifiez que le fichier `server.js` existe (voir exemple plus bas).

### Étapes à exécuter à chaque test

1. **Build production Angular**
   - Placez-vous dans `front/`.
   - Lancez :
     ```
     ng build --configuration production
     ```
   - Le front minifié sera généré dans `front/dist/front/browser`.
   - ⚠️ À chaque modification du front (HTML, CSS, Angular), il faut relancer cette commande pour mettre à jour la version minifiée.

2. **Lancer le backend Spring Boot**
   - Placez-vous dans `back/`.
   - Lancez :
     ```
     mvn spring-boot:run
     ```
   - Le backend écoute sur le port 8080.

3. **Lancer le serveur Express avec proxy**
   - Placez-vous dans `front/`.
   - Lancez :
     ```
     node server.js
     ```
   - Le front sera accessible sur http://localhost:8081/
   - Toutes les requêtes `/api` seront automatiquement proxy vers le backend sur 8080.

4. **Tester avec Lighthouse**
   - Ouvrez http://localhost:8081/user/login ou une page publique (c’est-à-dire accessible sans connexion, par exemple : page d’accueil, inscription, mentions légales, etc.).
   - Lancez Lighthouse depuis Chrome DevTools.
   - **Pour un audit pertinent, il est recommandé de tester chaque page de l’application (publique et privée). Seul un audit complet de toutes les pages a une réelle valeur.**
   - Vous obtiendrez le score réel de la version minifiée, avec backend accessible.

Exemple de fichier `server.js` :
```js
const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const path = require('path');

const app = express();
// Proxy /api vers le backend
app.use('/api', createProxyMiddleware({ target: 'http://localhost:8080', changeOrigin: true }));
// Servir les fichiers statiques Angular
app.use(express.static(path.join(__dirname, 'dist/front/browser')));
// Fallback Angular pour toutes les routes
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/front/browser/index.html'));
});
const PORT = 8081;
app.listen(PORT, () => {
  console.log(`Front prod avec proxy sur http://localhost:${PORT}`);
});
```

## Sécurité et persistance de session (Frontend)

- **Modes d’environnement Angular :**
	- **Production (`environment.prod.ts`)** : `production: true`, `useMock: false`, `enableDevRoutes: false`
		- Session via cookies HttpOnly sécurisés (aucune donnée de session côté JS).
	- **Développement/mock (`environment.dev.ts`)** : `production: false`, `useMock: true`, `enableDevRoutes: true`
		- Session stockée dans `localStorage` (pour tests locaux uniquement).
	- **Normal/intermédiaire (`environment.ts`)** : `production: false`, `useMock: false`, `enableDevRoutes: false`
		- Mocks et routes de dev désactivés, mais pas d’optimisation prod : la session fonctionne comme en prod (cookies HttpOnly, aucune persistance locale JS).
		- Sert pour des builds intermédiaires ou des tests manuels sans mocks.

- **Résumé :**
	- `useMock: true` → localStorage (mock/dev uniquement)
	- `useMock: false` → cookies HttpOnly (normal & prod)
	- Le mode "normal" est sécurisé comme la prod, sans mocks ni persistance locale.

- **Aucun risque de fuite de données de test/mock ou de persistance locale en production** : la configuration et le code garantissent l’isolation stricte.

Pour plus de détails, voir : `src/environments/`, `core/providers/data-sources.providers.ts`, `core/auth/auth.service.ts`, `core/auth/auth-mock.service.ts`.

---

## Tests et couverture

- **Backend** :
	- `./mvnw.cmd test` (generates reports in `target/`)
	- Jacoco coverage: 70% threshold per package (see `pom.xml` and `rules.md`)
- **Frontend** :
	- `npm run test:unit:ci` (Jest, unit tests + coverage)
	- `npm run e2e:ci` (Cypress, end-to-end tests + coverage)
	- **Attention : le backend doit être lancé avant d’exécuter les tests e2e.**

> ⚠️ Only these two commands are currently supported for frontend tests and coverage. Do not use `ng test` or `npm run test:e2e`.

## Documentation et références

- `agent.md` : rôle et comportement de l’agent assistant
- `rules.md` : règles de qualité, couverture, documentation
- `historique.md` : historique synthétique du projet et des actions de l’agent
- `historique.git.md` : log brut des commits git (générable automatiquement)
- `specs/` : spécifications fonctionnelles et techniques
- `postman/` : collection Postman pour l’API

## Notes

- Les entités JPA sont exclues de la couverture de code.
- Les variables sensibles (secrets JWT, accès DB) doivent être passées en variables d’environnement.
- Pour toute question sur les exigences, consulter `specs/`.


## FAQ – Problèmes fréquents

### "ng test" ne fonctionne pas / Erreur Jest builder

**Erreur courante :**
> Error: Could not find the '@angular-builders/jest:run' builder's node package.

**Solution :**
- Vérifiez que vous avez bien installé les dépendances : `npm install` dans le dossier `front/`.
- Si le problème persiste, installez le builder manquant : `npm install --save-dev @angular-builders/jest`
- Vous pouvez aussi lancer les tests unitaires directement avec Jest : `npm run test:unit` ou `npm run test:unit:ci`

### "ng" ou "npm" non reconnu

**Solution :**
- Vérifiez que Node.js et Angular CLI sont bien installés et présents dans votre PATH.
- Relancez votre terminal après installation.

### Problème de port déjà utilisé (frontend)

**Erreur courante :**
> Error: listen EADDRINUSE: address already in use 4200

**Solution :**
- Fermez l’application déjà en cours sur ce port ou lancez le front sur un autre port : `npm run start -- --port=4201`

### Problème de connexion à la base de données

**Solution :**
- Vérifiez les variables d’environnement (`DB_USER`, `DB_PASSWORD`, `DB_MDD_NAME`).
- Vérifiez que MySQL est bien démarré et accessible.

### Les tests Cypress échouent en CI

**Solution :**
- Vérifiez que le backend est bien lancé avant les tests e2e.
- Vérifiez la configuration du proxy (`proxy.conf.json`).
- Nettoyez les artefacts Cypress si besoin (`cypress/screenshots`, `cypress/videos`).

### Autres conseils
- Toujours relire les logs d’erreur pour repérer le vrai message bloquant.
- En cas de doute, supprimez `node_modules` et refaites `npm install`.
- Consultez `rules.md` pour les conventions et seuils qualité.

## Documentation et références

- `agent.md` : rôle et comportement de l’agent assistant
- `rules.md` : règles de qualité, couverture, documentation
- `historique.md` : historique synthétique du projet et des actions de l’agent
- `historique.git.md` : log brut des commits git (générable automatiquement)
- `specs/` : spécifications fonctionnelles et techniques
- `postman/` : collection Postman pour l’API

## Notes

- Les entités JPA sont exclues de la couverture de code.
- Les variables sensibles (secrets JWT, accès DB) doivent être passées en variables d’environnement.
- Pour toute question sur les exigences, consulter `specs/`.


## FAQ – Problèmes fréquents
- **Aucun risque de fuite de données de test/mock ou de persistance locale en production** : la configuration et le code garantissent l’isolation stricte.

Pour plus de détails, voir : `src/environments/`, `core/providers/data-sources.providers.ts`, `core/auth/auth.service.ts`, `core/auth/auth-mock.service.ts`.
