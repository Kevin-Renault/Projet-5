# Gestion de l'environnement Spring Boot

Le profil actif de l'application backend est défini par la propriété `spring.profiles.active`.

- Par défaut, la valeur est `dev` (voir `back/src/main/resources/application.properties`).
- Vous pouvez la surcharger via une variable d'environnement, un argument JVM ou un autre fichier de configuration.
- Exemple : pour lancer en mode production : `SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run`

Si aucune valeur n'est définie, le profil `dev` sera utilisé par défaut.

# Application Full-Stack MDD-API

Projet OpenClassrooms : API et Frontend pour un réseau social de développeurs.

## Structure du projet

- `back/` : API Java Spring Boot (src/main/java, src/main/resources, tests, pom.xml)
- `front/` : Application Angular (src/, tests, configuration, package.json)
- `specs/` : Spécifications fonctionnelles et techniques (PDF/Markdown)
- `postman/` : Collection Postman pour tester l’API
- Fichiers de documentation : `agent.md`, `rules.md`, `historique.md`, `historique.git.md`

## Prérequis

- Java 21+
- Node.js 18+
- MySQL 8+

## Installation et configuration

1. **Base de données** :
	 - Exécutez `back/src/main/resources/base.sql` dans MySQL pour créer la base et l’utilisateur.
	 - Définissez les variables d’environnement : `DB_USER`, `DB_PASSWORD`, `DB_MDD_NAME`.
2. **Backend** :
	 - Placez-vous dans `back/`.
	 - Lancez `./mvnw.cmd clean package` (Windows) ou `./mvnw clean package` (Linux/Mac).
	 - Fichier de config : `src/main/resources/application.properties`.
3. **Frontend** :
	 - Placez-vous dans `front/`.
	 - Lancez `npm install` puis `npm run start` (ou `npm run start:e2e` pour le port 4201 avec proxy).

## Lancement

- **Backend** :
	- `cd back`
	- `./mvnw.cmd spring-boot:run` (Windows) ou `./mvnw spring-boot:run`
	- L’API écoute sur `http://localhost:8080/api`
- **Frontend** :
	- `cd front`
	- `npm run start` (http://localhost:4200) ou `npm run start:e2e` (http://localhost:4201)

## Tests et couverture

- **Backend** :
	- `./mvnw.cmd test` (generates reports in `target/`)
	- Jacoco coverage: 70% threshold per package (see `pom.xml` and `rules.md`)
- **Frontend** :
	- `npm run test:unit:ci` (Jest, unit tests + coverage)
	- `npm run e2e:ci` (Cypress, end-to-end tests + coverage)

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
