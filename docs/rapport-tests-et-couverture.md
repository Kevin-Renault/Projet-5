# Rapport de tests & couverture

Objectif : fournir un livrable **lisible** (et non “juste” des fichiers dans `target/` / `coverage/`) qui indique :
- quelles commandes lancer,
- où sont les rapports localement,
- quels sont les **résultats chiffrés** disponibles,
- quels artefacts sont téléchargeables facilement sur GitHub (CI).

---

## 1) Résumé (à partir des rapports locaux)

Les chiffres ci-dessous sont extraits des fichiers de rapports présents dans le workspace.

- Backend JaCoCo (`back/target/site/jacoco/jacoco.xml`) : dernière génération constatée `2026-02-27T16:10:28`
- Backend Surefire (`back/target/surefire-reports/TEST-*.xml`) : dernière génération constatée `2026-02-27T16:10:27`
- Front Jest + couverture (`front/jest-results.json`, `front/coverage/coverage-summary.json`) : dernière génération constatée `2026-02-27T20:46:23`
- E2E Cypress + couverture (`front/cypress/results/junit-*.xml`, `front/coverage-e2e/index.html`) : dernière génération constatée `2026-03-01T18:43:47Z`

### 1.1 Backend — Tests (JUnit/Surefire)

- Suites : 20
- Tests : 81
- Échecs (`failures`) : 0
- Erreurs (`errors`) : 0
- Ignorés (`skipped`) : 0

Source : `back/target/surefire-reports/TEST-*.xml`

### 1.2 Backend — Couverture (JaCoCo)

| Mesure | Couverture | Couvert | Manqué | Total |
|---|---:|---:|---:|---:|
| Lignes | 94,55% | 694 | 40 | 734 |
| Branches | 78,28% | 191 | 53 | 244 |
| Instructions | 94,23% | 3 071 | 188 | 3 259 |
| Méthodes | 94,71% | 161 | 9 | 170 |
| Classes | 100,00% | 40 | 0 | 40 |

Sources :
- HTML : `back/target/site/jacoco/index.html`
- XML : `back/target/site/jacoco/jacoco.xml`

### 1.3 Frontend — Tests unitaires (Jest)

- Suites : 31 (31 passées, 0 échouée)
- Tests : 67 (67 passés, 0 échoué, 0 pending)

Source : `front/jest-results.json`

### 1.4 Frontend — Couverture (Jest/Istanbul)

| Mesure | Couverture | Couvert | Total |
|---|---:|---:|---:|
| Lignes | 90,74% | 696 | 767 |
| Statements | 90,00% | 792 | 880 |
| Fonctions | 85,09% | 234 | 275 |
| Branches | 79,14% | 129 | 163 |

Sources :
- HTML : `front/coverage/lcov-report/index.html`
- Synthèse JSON : `front/coverage/coverage-summary.json`

### 1.5 E2E — Cypress

Dernier run Cypress constaté (local) :
- JUnit : `front/cypress/results/junit-f13cbc736982d1920bbc75fa93612ed9.xml`
- Suites : 2
- Tests : 1
- Échecs (`failures`) : 0
- Erreurs (`errors`) : 0
- Ignorés (`skipped`) : 0

Couverture E2E (NYC/Istanbul) — globale :

| Mesure | Couverture | Couvert | Total |
|---|---:|---:|---:|
| Statements | 84,57% | 466 | 551 |
| Branches | 77,04% | 188 | 244 |
| Fonctions | 79,80% | 166 | 208 |
| Lignes | 85,77% | 392 | 457 |

Sources :
- HTML : `front/coverage-e2e/index.html` (généré le `2026-03-01T18:43:47Z`)
- LCov : `front/coverage-e2e/lcov.info`
- Données brutes : `front/.nyc_output/out.json`

Remarque : Cypress est configuré pour générer un rapport JUnit (XML) à chaque run via `mocha-junit-reporter` (dans `front/cypress/results/`).

---

## 2) Générer / régénérer les rapports en local (Windows)

### 2.1 Backend (Spring Boot) — tests + JaCoCo

Depuis le dossier `back/` :

```powershell
# build + tests + JaCoCo
.\mvnw.cmd -B verify
```

Sorties attendues :
- tests : `back/target/surefire-reports/`
- couverture : `back/target/site/jacoco/index.html`

Remarque : les tests d’intégration démarrent une base MySQL (selon votre config), donc il faut que l’environnement DB soit disponible (cf. README).

### 2.2 Frontend (Angular) — tests unitaires Jest + couverture

Depuis le dossier `front/` :

```powershell
npm ci
npm run test:unit:ci
```

Sorties attendues :
- `front/jest-results.json`
- `front/coverage/lcov-report/index.html`
- `front/coverage/coverage-summary.json`

### 2.3 Frontend — Cypress E2E (+ couverture)

Périmètre : tests end-to-end du front (parcours utilisateur) avec génération de la couverture E2E.

Commande recommandée (1 commande, depuis `front/`) :

```powershell
npm run e2e:run:coverage
```

Ce script :
- lance le frontend en mode instrumenté,
- exécute Cypress en headless,
- génère le rapport de couverture E2E.

Pré-requis (selon votre machine) :
- backend accessible (ex. `http://localhost:8080`) si les specs E2E ne mockent pas toutes les routes,
- port `4200` disponible.

Alternative (sans couverture, si le frontend tourne déjà) :

```powershell
npm run cypress:run
```

Sorties potentielles (selon configuration Cypress/coverage) :
- `front/cypress/screenshots/`
- `front/cypress/videos/` (désactivé par défaut via `video: false`)
- `front/cypress/results/` (rapport JUnit XML)
- `front/coverage-e2e/` et/ou `front/.nyc_output/` (si instrumentation + extraction de couverture E2E)

Rapport de couverture E2E :
- HTML : `front/coverage-e2e/index.html`
- LCov : `front/coverage-e2e/lcov.info`

---

## 3) Ce qui est facilement disponible sur GitHub (CI)

Workflow concerné : `.github/workflows/sonarcloud-modular.yml` (nom : **SonarCloud Modular**).

### 3.1 Artefacts uploadés

Le workflow publie des artefacts téléchargeables depuis l’onglet **Actions** (sur un run donné), avec `retention-days: 14` :

- `backend-jar`
  - `back/target/mdd-api-0.0.1-SNAPSHOT.jar`

- `jacoco-report`
  - `back/target/site/jacoco/` (rapport HTML complet)

- `jest-report`
  - `front/coverage/` (rapport HTML + JSON)
  - `front/jest-results.json`

- `cypress-report`
  - `front/cypress/results`
  - `front/cypress/screenshots`
  - `front/cypress/videos`
  - `front/coverage-e2e`
  - `front/.nyc_output`

Remarque : pour `jest-report` et `cypress-report`, le workflow est tolérant (`if-no-files-found: warn`) — donc l’artefact peut être incomplet si un dossier n’a pas été produit.

### 3.2 Résumé “lisible” dans GitHub Actions

Le job `report` télécharge les artefacts et génère un **GITHUB_STEP_SUMMARY** (visible directement dans l’UI du run) avec :
- un tableau de couverture backend (JaCoCo) extrait de `jacoco.xml`,
- un résumé des tests unitaires front + couverture (Jest),
- un best-effort sur Cypress (si un XML JUnit est trouvé),
- la liste des artefacts.

### 3.3 Variables et secrets en CI (point utile pour rejouer les tests)

- Le workflow génère un `JWT_SECRET` à chaque run (`Generate JWT secret (CI)`), ce qui évite d’avoir un secret commité.
- Le backend en CI utilise une base MySQL service (container) avec :
  - `MYSQL_DATABASE=mdd`
  - `MYSQL_ROOT_PASSWORD=root`

---

## 4) Notes / limites actuelles

- Les chiffres E2E (Cypress) ne sont pas “garantis” localement tant que `front/cypress/results/` n’est pas généré (et actuellement la config ne montre pas explicitement un reporter JUnit).
- Les pourcentages affichés ici sont ceux des rapports présents dans le repo au moment de la rédaction (ils peuvent changer dès que les tests sont relancés).
