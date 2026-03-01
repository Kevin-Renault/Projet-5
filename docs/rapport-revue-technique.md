# Rapport de revue technique — Application full‑stack MDD (Option B)

Date : 2026‑03‑01  
Périmètre : code présent dans ce dépôt (dossiers `back/` et `front/`) + pipeline CI GitHub Actions.

## 1) Résumé exécutif

### Points forts (ce qui est déjà solide)
- **Sécurité d’authentification orientée “SPA + cookies”** : JWT en cookie **HttpOnly**, refresh token **opaque** persisté en base avec **hash** + **rotation** (suppression de l’ancien token au refresh). Voir `back/src/main/java/.../security/RefreshTokenService.java`, `RefreshTokenEntity.java`, `AuthController.java`.
- **Protection CSRF côté back + mécanisme côté front** : endpoint `GET /api/auth/csrf` + header `X-XSRF-TOKEN`, et interceptor Angular qui ajoute `X-XSRF-TOKEN` sur méthodes unsafe + `withCredentials=true`. Voir `SecurityConfig.java`, `AuthController.java`, `front/src/app/core/Interceptor/CredentialsInterceptor.ts`, `front/src/app/core/auth/auth.service.ts`.
- **Qualité “contrôlée” par la CI** : `mvn verify` (tests + JaCoCo), Jest (coverage), Cypress E2E, artifacts des rapports. Voir `.github/workflows/sonarcloud-modular.yml`.
- **Gestion d’erreurs API homogène** via `GlobalExceptionHandler` (validation, conflits DB, 400/401/403/500). Voir `back/src/main/java/.../exception/GlobalExceptionHandler.java`.

### Décisions / options & actions associées
- Les **risques**, **options** (A/B/…) et **actions** liées aux décisions produit/tech (environnements, déploiement/CORS, cookies `Secure`/`SameSite`, auth multi‑clients, validation email OTP, modération/droits, etc.) sont consolidés dans `docs/options-et-actions-techniques.md`.
- Ce rapport reste volontairement centré sur l’**audit factuel de l’existant** (constats techniques, implémentation, tests/CI).

## 2) Méthode / grille de revue

- Lecture ciblée : sécurité (`SecurityConfig`, `Jwt*`, `AuthController`, refresh token), gestion des erreurs, configuration runtime, CI, scripts et points d’intégration front.
- Objectif : produire un rapport de **constats**, **risques**, **axes d’amélioration** et un **plan d’actions**.

## 3) Architecture (vue d’ensemble)

### Découpage
- `front/` : SPA Angular, appels API via `HttpClient`, interceptors, tests Jest + Cypress.
- `back/` : API Spring Boot 3.2 (Java 21), Spring Security stateless, JPA + MySQL, OpenAPI/Swagger.

### Flux “session” (résumé)
- Initialisation CSRF : le front appelle `GET /api/auth/csrf` et stocke `X-XSRF-TOKEN` (header) en mémoire (`CsrfTokenService`).
- Login/register : POST vers `/api/auth/login|register` avec `withCredentials`; le back pose cookies access/refresh (+ XSRF‑TOKEN).
- Requêtes API : interceptor ajoute `withCredentials=true` et header `X-XSRF-TOKEN` sur méthodes unsafe.
- Expiration access token : `RefreshOn401Interceptor` déclenche `POST /api/auth/refresh` une seule fois, puis rejoue la requête initiale.

Références : `front/src/app/core/auth/auth.service.ts`, `CredentialsInterceptor.ts`, `RefreshOn401Interceptor.ts`, `AuthController.java`.

## 4) Backend — Revue technique

### 4.1 Dépendances & base technique
- Spring Boot 3.2.0, Java 21 (via Maven `maven-compiler-plugin` en release 21). Voir `back/pom.xml`.
- Dépendances principales : Web, Security, Validation, Data JPA, MySQL, OpenAPI UI, JJWT.

Constat : stack moderne et cohérente pour un projet full‑stack.

### 4.2 Sécurité (Spring Security)

#### Authentification
- Filtre `JwtAuthenticationFilter` :
  - Priorité : ne pas ré‑authentifier si un `Authentication` non‑anonymous est déjà présent.
  - Source du token : `Authorization: Bearer …` **ou** cookie `access_token` (configurable).
  - Subject JWT attendu : `userId` numérique ; charge l’utilisateur depuis `MddUserRepository`.

Points positifs :
- Compatible “API + SPA” (Bearer possible, cookies possibles).
- Ne met pas de rôles/authorities (simple) : ok si le projet ne gère pas de RBAC.

Points à surveiller :
- Absence d’authorities : si des règles d’accès plus fines deviennent nécessaires, il faudra enrichir le principal et/ou les claims.

Référence : `back/src/main/java/.../security/JwtAuthenticationFilter.java`.

#### CSRF
- Activation CSRF + `CookieCsrfTokenRepository` (`cookiePath=/`).
- Endpoint `GET /api/auth/csrf` “touche” le token pour forcer l’émission du cookie et renvoie le token en header `X-XSRF-TOKEN`.

Points positifs :
- Pattern robuste pour SPA quand le token ne peut pas être lu en cookie (HttpOnly).

Points à surveiller :
- En multi‑origines, il faut s’assurer que `X-XSRF-TOKEN` est lisible côté navigateur (CORS `Access-Control-Expose-Headers`), sinon le front ne pourra pas stocker le token.

Références : `SecurityConfig.java`, `AuthController.java`.

#### Cookies JWT / refresh token
- `JwtCookieService` :
  - access cookie : path `/`, HttpOnly, `maxAge=expirationSeconds`.
  - refresh cookie : path **restreint** à `/api/auth`, HttpOnly, `maxAge=refreshExpirationSeconds`.
  - paramètres : `cookie-secure`, `sameSite`, noms de cookies configurables.

Points positifs :
- Limitation de surface d’attaque : refresh cookie envoyé uniquement sur `/api/auth/*`.

Point critique “prod” :
- `security.jwt.cookie-secure` par défaut `false` (dev OK, prod non). Prévoir profil prod.

Référence : `JwtCookieService.java`, `application.properties`.

#### Refresh token persistant + rotation
- Refresh token opaque : 32 bytes aléatoires, base64url.
- Stockage DB : **hash SHA‑256** en base (pas le token en clair), expiration `expires_at`.
- Rotation : vérifie existence + expiration, supprime l’ancien, crée un nouveau (transactionnel).

Points positifs :
- Bon niveau de sécurité pour un refresh token : hash + rotation + révocation.

Référence : `RefreshTokenService.java`, `RefreshTokenEntity.java`, `schema.sql`.

### 4.3 Validation & API contract
- DTOs `LoginRequest` / `RegisterRequest` annotés `jakarta.validation`.
- Politique mot de passe : regex exigeant maj/min/chiffre/spécial, longueur min 8.

Points positifs :
- Validation serveur explicite, erreurs 400 cohérentes.

Point à surveiller :
- Regex password : ok pour une contrainte “OC”, mais peut être trop stricte en produit (support UX). Documenter côté front.

Référence : `back/src/main/java/.../dto/auth/RegisterRequest.java`.

### 4.4 Gestion d’erreurs
- `GlobalExceptionHandler` centralise les erreurs :
  - `MethodArgumentNotValidException` → 400 + détail par champ
  - `DataIntegrityViolationException` → 409
  - `AccessDeniedException` → 403
  - fallback → 500

Points positifs :
- Format stable et prédictible pour le front.

Référence : `GlobalExceptionHandler.java`.

### 4.5 Données / persistance
- Schéma SQL explicite (`schema.sql`) : topics, users, subscriptions, articles, comments, refresh_token.
- Table `refresh_token` : contraintes `UNIQUE(user_id)` et `UNIQUE(token_hash)`.

Point positif :
- Le modèle de refresh token est cohérent avec la rotation (1 token actif / user).

Point à surveiller :
- Les règles métiers (longueur titre, contenu, etc.) doivent être alignées entre DTOs, DB (`VARCHAR(100)`) et front (validators).

## 5) Frontend — Revue technique

### 5.1 Intégration HTTP, cookies et CSRF
- `CredentialsInterceptor` :
  - force `withCredentials=true` sur toutes requêtes.
  - ajoute `X-XSRF-TOKEN` uniquement pour méthodes unsafe et URLs contenant `/api/`.

Point positif :
- Approche simple et efficace pour éviter l’oubli `withCredentials`.

Point à surveiller :
- Le test `req.url.includes('/api/')` suppose que toutes les routes API contiennent `/api/` et que les URLs externes n’en contiennent pas.

Référence : `front/src/app/core/Interceptor/CredentialsInterceptor.ts`.

### 5.2 Refresh automatique
- `RefreshOn401Interceptor` :
  - ignore les endpoints auth pour éviter les boucles.
  - mutualise un refresh en cours via `shareReplay(1)`.
  - en cas d’échec refresh : `clearSession()`.

Point positif :
- Pattern propre pour éviter “N refresh en parallèle”.

Référence : `front/src/app/core/Interceptor/RefreshOn401Interceptor.ts`.

### 5.3 Gestion de session
- `AuthService.initSession()` :
  - tente `/csrf` (non bloquant)
  - tente `/me`, sinon refresh puis `/me`, sinon “Session expirée”.

Point positif :
- Stratégie claire pour restaurer une session au chargement.

Référence : `front/src/app/core/auth/auth.service.ts`.

### 5.4 Dépendances
- Angular `^19.2.18`.
- Angular Material `^18.2.14`.

Risque :
- Le mix de majors peut générer des erreurs de build/runtime ou des typings incohérents.

Référence : `front/package.json`.

## 6) Tests, couverture et CI

### 6.1 Backend
- JaCoCo : génération HTML + XML au `verify`.
- Seuil minimum (PACKAGE) : 70% line/branch/instruction, exclusions `**/entity/**`.

Point positif :
- Le build casse si couverture insuffisante (garde‑fou utile).

Référence : `back/pom.xml`.

### 6.2 Frontend
- Jest en CI : `npm run test:unit:ci` (coverage + json report).
- Cypress E2E : `npm run cypress:run`.

Référence : `front/package.json`, `.github/workflows/sonarcloud-modular.yml`.

### 6.3 Pipeline CI (GitHub Actions)
- Jobs séparés : backend, frontend, cypress, report.
- MySQL en service pour backend tests et E2E.
- Génération d’un `JWT_SECRET` aléatoire en CI (évite de dépendre d’un secret commité).

Point positif :
- Pipeline complet et reproductible (unit + integration + e2e).

Point à surveiller :
- Cypress est conditionné au fait que `GET /api/env` contienne `"dev"` ; en cas de changement de réponse, le job peut “skip” les E2E sans échec.

Référence : `.github/workflows/sonarcloud-modular.yml`.

## 7) Références internes (fichiers)

- Sécurité back :
  - `back/src/main/java/com/openclassrooms/mddapi/config/SecurityConfig.java`
  - `back/src/main/java/com/openclassrooms/mddapi/controller/AuthController.java`
  - `back/src/main/java/com/openclassrooms/mddapi/security/JwtAuthenticationFilter.java`
  - `back/src/main/java/com/openclassrooms/mddapi/security/JwtCookieService.java`
  - `back/src/main/java/com/openclassrooms/mddapi/security/RefreshTokenService.java`
- Config : `back/src/main/resources/application.properties`
- DB : `back/src/main/resources/schema.sql`
- Front auth :
  - `front/src/app/core/auth/auth.service.ts`
  - `front/src/app/core/auth/csrf-token.service.ts`
  - `front/src/app/core/Interceptor/CredentialsInterceptor.ts`
  - `front/src/app/core/Interceptor/RefreshOn401Interceptor.ts`
- CI : `.github/workflows/sonarcloud-modular.yml`
- Couverture : `back/pom.xml`
