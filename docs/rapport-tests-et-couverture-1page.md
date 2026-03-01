# Rapport tests & couverture — 1 page (synthèse)

Périmètre : backend (JUnit/Surefire + JaCoCo) + frontend (Jest/Istanbul) + E2E (Cypress).

Date des chiffres (rapports locaux présents dans le repo) :
- Backend : 2026-02-27T16:10 (tests + JaCoCo)
- Frontend : 2026-02-27T20:46 (tests + couverture)

---

## Résultats clés

### Backend (Spring Boot)

Tests (Surefire JUnit) :
- 81 tests, 0 échec, 0 erreur, 0 ignoré
- Source : `back/target/surefire-reports/TEST-*.xml`

Couverture (JaCoCo) :
- Lignes : 94,55% (694/734)
- Branches : 78,28% (191/244)
- Instructions : 94,23% (3 071/3 259)
- Source : `back/target/site/jacoco/jacoco.xml` + HTML `back/target/site/jacoco/index.html`

### Frontend (Angular)

Tests unitaires (Jest) :
- 67 tests, 0 échec (31 suites)
- Source : `front/jest-results.json`

Couverture (Istanbul) :
- Lignes : 90,74% (696/767)
- Statements : 90,00% (792/880)
- Fonctions : 85,09% (234/275)
- Branches : 79,14% (129/163)
- Source : `front/coverage/coverage-summary.json` + HTML `front/coverage/lcov-report/index.html`

### E2E (Cypress)

- Local : si les E2E sont rejoués, un rapport JUnit XML est attendu dans `front/cypress/results/`.
- Couverture E2E : après exécution E2E avec frontend instrumenté, le rapport est généré via `npm run e2e:coverage` et se trouve dans `front/coverage-e2e/index.html`.
- CI : un artefact `cypress-report` est publié (résultats, screenshots, coverage e2e…). Le workflow sert le frontend via `serve-e2e-coverage` pour permettre la couverture E2E.

---

## Comment régénérer (3 commandes)

Backend (depuis `back/`) :

```powershell
.\mvnw.cmd -B verify
```

Frontend unit (depuis `front/`) :

```powershell
npm ci
npm run test:unit:ci
```

E2E (depuis `front/`, optionnel) :

```powershell
npm run cypress:run
```

---

## Où trouver ça sur GitHub (Actions)

Workflow : `.github/workflows/sonarcloud-modular.yml`.

Dans un run GitHub Actions :
- Onglet **Summary** : le job `report` publie un résumé lisible (tables + compteurs), basé sur les artefacts téléchargés.
- Onglet **Artifacts** :
  - `jacoco-report` (HTML backend)
  - `jest-report` (coverage + `jest-results.json`)
  - `cypress-report` (best-effort)
  - `backend-jar`

Note CI : `JWT_SECRET` est généré à chaque run (pas de secret commité).

---

Référence : version détaillée dans `docs/rapport-tests-et-couverture.md`.
