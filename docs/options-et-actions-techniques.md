# Options & actions techniques — reprise du projet

Date : 2026‑03‑01  
Objectif : document de reprise pour un chef de projet / tech lead et l’équipe.  
Format : pour chaque sujet, **options possibles** + **impacts** + **actions concrètes**.

> Portée : application full‑stack Angular + Spring Boot (JWT en cookies HttpOnly + refresh token DB + CSRF).

---

## 1) Environnements, profils Spring et configuration

### Constat (état actuel)
- Deux fichiers de config :
  - `back/src/main/resources/application.properties` (base)
  - `back/src/main/resources/application-prod.properties` (surcharge pour profil prod)
- Les paramètres DB/JWT sont injectés via variables d’environnement.

### Décision à prendre
**Comment piloter les environnements** (dev/ci/staging/prod) ?

### Options
**Option A — Profil piloté par l’environnement (recommandé)**
- Le profil est choisi au runtime via : `SPRING_PROFILES_ACTIVE=dev|prod` ou `--spring.profiles.active=prod`.
- Les fichiers `application-<profil>.properties` ne contiennent pas `spring.profiles.active`.

**Option B — Profil “fixé” dans `application.properties`**
- `spring.profiles.active=dev` dans le fichier de base.
- Le passage en prod repose sur surcharge explicite (env/args) au déploiement.

### Impacts
- A : prévisible en CI/CD, limite les surprises, compatible multi‑environnements.
- B : simple en local, mais risque de “prod lancée en dev” si oubli de surcharge.

### Actions (quel que soit le choix)
- Définir une matrice de config (dev/ci/staging/prod) :
  - DB (hôte, user, password)
  - init SQL (oui/non)
  - logs SQL (oui/non)
  - cookies (Secure/SameSite)
- Documenter les commandes de run par profil (Maven / jar).

---

## 2) Topologie de déploiement & CORS

### Décision à prendre
**Front et back seront‑ils servis sur le même domaine ?**

### Options
**Option A — Même “site” (simple)**
- Ex : `https://app.example.com` sert le front et reverse‑proxy `/api` vers le back.

**Option B — Domaines séparés**
- Ex : `https://front.example.com` et `https://api.example.com`.

### Impacts
- A : cookies et CSRF plus simples, moins de contraintes navigateur.
- B : nécessite une stratégie CORS, et réglages cookies plus stricts.

### Actions
- Option A :
  - Mettre en place un reverse‑proxy (Nginx/Traefik) `/api -> back`.
  - Confirmer que l’app fonctionne sans CORS.
- Option B :
  - Définir la politique CORS (origins, credentials, expose headers).
  - Tester le flux CSRF : le front doit pouvoir lire le header `X-XSRF-TOKEN`.

---

## 3) Cookies JWT (Secure/SameSite) & HTTPS

### Décision à prendre
**Contraintes HTTPS et “cross‑site”**.

### Options
**Option A — Cookies “same‑site”**
- Si front/back même site : `SameSite=Lax` est généralement acceptable.

**Option B — Cookies “cross‑site”**
- Si domaines différents et besoin de cookies : `SameSite=None` + `Secure=true`.

### Impacts
- `Secure=true` implique HTTPS (sinon cookies non envoyés).
- `SameSite=None` est requis dans certains scénarios cross‑site.

### Actions
- Créer/valider un profil `prod` et/ou `staging` :
  - `security.jwt.cookie-secure=true`
  - `security.jwt.cookie-samesite=<Lax|None>` selon topologie
- Vérifier le comportement en navigateur (Chrome/Safari) et en navigation interne.

---

## 4) Stratégie d’authentification multi‑clients (web, mobile, desktop)

### Décision à prendre
**Le produit doit‑il supporter d’autres clients que le navigateur web ?**

### Options
**Option A — Cookies HttpOnly uniquement (web‑first)**
- Le navigateur gère les cookies, CSRF reste pertinent.

**Option B — Bearer token pour clients natifs (hybride)**
- Web garde cookies HttpOnly.
- Mobile/desktop consomme un token via `Authorization: Bearer`.

**Option C — Bearer token partout**
- Simplifie multi‑clients mais augmente le risque XSS (stockage tokens côté JS).

### Impacts
- A : meilleure posture sécurité web (si XSS), mais moins naturel côté clients natifs.
- B : flexible mais complexifie la maintenance (2 modes).
- C : plus simple côté API mais exige un fort niveau de sécurisation front (CSP, durcissement XSS).

### Actions
- Si B : définir contractuellement quels endpoints acceptent bearer vs cookies.
- Définir une politique de rotation/expirations et de révocation.

---

## 5) Validation email (compte réel) — fonctionnalité “vital à terme”

### Décision à prendre
**Quel mécanisme de vérification email ?**

### Position : voici ce que je préconise en termes de bonne pratique, dev/utilisateur

- **Pas de liens cliquables dans les emails** (validation ou reset). Objectif : ne pas normaliser un geste qui ressemble aux patterns de phishing.
- La résistance au brute force ne vient pas du “lien vs code”, mais des **contre‑mesures serveur** : expiration courte, limites de tentatives, rate limiting, verrouillage temporaire, tokens non réutilisables.
- En conséquence : privilégier un flow **OTP (copier/coller)**, avec durcissement côté API.

### Options
**Option A — Code OTP (copier/coller)**
- Envoi d’un code (ex : 6 chiffres) à saisir.

**Option B — Lien de validation (token signé)**
- Envoi d’une URL unique expirante.

### Impacts
- OTP : UX simple, mais nécessite anti‑bruteforce (tentatives limitées).
- Lien : UX “1 clic”, nécessite gestion de token et redirection front.

### Impacts (si OTP imposé)
- Nécessite une UI dédiée “Saisir le code reçu par email” + gestion des erreurs (code invalide/expiré/trop d’essais).
- Nécessite un mécanisme de renvoi de code (“Resend”) avec délai (cooldown) et limites.

### Actions (communes)
- Ajouter un état sur l’utilisateur : `email_verified` (false par défaut).
- Ajouter une table de tokens de vérification :
  - `user_id`, `token_hash`, `expires_at`, `attempts`, `created_at`
- Endpoints :
  - `POST /api/auth/request-email-verification` (renvoi)
  - `POST /api/auth/verify-email` (consommation code/token)
- Sécurité :
  - limiter le rate (par IP et par compte)
  - ne pas révéler si l’email existe
  - invalider le token après succès

### Critères d’acceptation
- Un compte non vérifié ne peut pas (selon choix produit) : publier / commenter / s’abonner.
- Les tokens expirent et ne sont pas réutilisables.

---

## 6) Réinitialisation mot de passe (complément attendu après email validation)

### Décision à prendre
**Quel mécanisme de réinitialisation mot de passe ?**

### Position : voici ce que je préconise en termes de bonne pratique, dev/utilisateur
- Pas de liens cliquables dans les emails (reset).
- La résistance au brute force dépend des contre‑mesures serveur (expiration, limites d’essais, rate limiting), pas du format lien vs code.
- En conséquence : privilégier un flow **OTP (copier/coller)**.

### Options
**Option A — Reset par lien**
- Token unique expirant, page front de reset.

**Option B — Reset par code OTP**
- Plus proche du flow “code mail”.

### Impacts (si OTP imposé)
- Nécessite une UI dédiée “Saisir le code reçu par email” + gestion des erreurs (code invalide/expiré/trop d’essais).
- Nécessite un mécanisme de renvoi de code (“Resend”) avec délai (cooldown) et limites.

### Actions
- Endpoints : `POST /api/auth/forgot-password`, `POST /api/auth/reset-password`.
- Invalidation des sessions : révoquer refresh token(s) après reset.

---

## 7) Gestion des secrets et variables en CI/CD

### Constat
- En CI, DB est injectée au job via variables (ex : user/password). Les JWT_* ont des defaults.

### Décision à prendre
**Où stocker et comment injecter les valeurs sensibles ?**

### Options (GitHub Actions)
**Option A — Secrets/Variables repository (recommandé)**
- `DB_PASSWORD` en secret, `DB_USER/DB_MDD_NAME` en variables.

**Option B — Saisie manuelle via `workflow_dispatch` inputs**
- À éviter pour les secrets (risque de fuite dans logs et métadonnées).

### Actions
- Lister les variables sensibles (mots de passe, clés) et les mettre en “secrets”.
- Lister les variables non sensibles (durées, flags) et les mettre en “vars”.

---

## 8) Observabilité, conformité et exploitation

### Options
**Option A — Logs applicatifs simples**
- Suffisant pour un MVP.

**Option B — Observabilité structurée**
- Correlation ID, métriques, dashboards.

### Actions
- Définir les événements à logguer (login/logout/refresh/verify-email) sans PII sensible.
- Ajouter un `correlationId` par requête (header) si besoin.

---

## 9) Sécurité “hardening” (recommandations génériques)

### Options
**Option A — Hardening minimal**
- Rate limiting sur auth + messages d’erreur neutres.

**Option B — Hardening renforcé**
- Backoff progressif, lock temporaire, alerting.

### Actions
- Rate limit sur `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`.
- Tests de non‑régression sécurité (XSRF requis sur méthodes unsafe, cookies ok).

---

## 10) Règles fonctionnelles & conformité (réseau social)

### Contexte
L’application manipule du **contenu utilisateur** (articles, commentaires, topics/abonnements). La reprise du projet doit trancher des règles fonctionnelles (édition/suppression/modération) qui ont des impacts techniques (données, droits, UI) et potentiellement des impacts conformité **selon la juridiction** (ex : gestion des données personnelles, droit à l’effacement, obligations de modération/notification selon le contexte légal applicable).

### Décisions à prendre
1) Quelles règles d’**édition/suppression** pour : article, commentaire, topic ?  
2) Faut‑il une **modération** (signalement, masquage, bannissement) ?  
3) Quel modèle de **droits** (ownership simple vs rôles) et où stocker l’autorisation (JWT vs DB) ?

### 10.1 Articles et commentaires : modification / suppression

#### Options (suppression)
**Option A — Suppression logique (soft delete) (souvent préférable)**
- Le contenu est marqué supprimé (`deleted_at`, `deleted_by`, `delete_reason`) et n’est plus visible en UI.
- Permet audit, restauration, et évite de casser des références.

**Option B — Suppression physique (hard delete)**
- Ligne supprimée en base.
- Plus simple conceptuellement, mais complique audit et gestion des références (et peut être incompatible avec certaines politiques internes).

#### Options (modification)
**Option A — Modification sans historique**
- On met à jour `content`, on garde `updated_at`.

**Option B — Modification avec historique**
- Table d’historique (versions) ou champ `previous_content`.
- Permet transparence/modération (au prix de complexité et stockage).

#### Règles à définir (factuelles)
- Qui peut modifier/supprimer : auteur uniquement ? auteur + modérateur ? admin ?
- Fenêtre temporelle : modification autorisée 10 min / 1 h / illimité ?
- Effet en UI : afficher “supprimé” (placeholder) vs cacher totalement.

#### Actions techniques
- Étendre le schéma : ajouter `updated_at`, `deleted_at` (et éventuellement `deleted_by`, `edit_count`).
- Ajouter endpoints : `PATCH/PUT /articles/{id}`, `DELETE /articles/{id}`, idem commentaires.
- Ajouter contrôles d’autorisation (voir §10.3) + tests unitaires/intégration.

### 10.2 Topics : suppression / modification

#### Problème
Un topic a des **abonnements** et des **articles** : la suppression a un impact de cohérence.

#### Options
**Option A — Interdire la suppression** (topic immuable)
- Le topic n’est jamais supprimé, seulement renommé/archivé.

**Option B — Archiver**
- Champ `archived_at` : plus de nouveaux articles, mais l’historique reste visible.

**Option C — Supprimer**
- Nécessite une stratégie : suppression cascade des articles ? migration vers “topic inconnu” ? blocage si contenu existe ?

#### Actions techniques
- Ajouter une règle métier explicite côté API + UI admin/modération si nécessaire.
- Ajouter tests de cohérence (articles/abonnements existants).

### 10.3 Gestion des droits (actions et accès)

#### Options (modèle de droits)
**Option A — Ownership uniquement**
- L’auteur peut modifier/supprimer son contenu. Pas de rôles.
- Simplicité maximale.

**Option B — Rôles (user/moderator/admin)**
- Ajoute des capacités : modérer, supprimer/éditer contenu tiers, gérer topics.

**Option C — Permissions fines (ACL/ABAC)**
- Permissions par action/ressource. Complexe, à réserver si besoin.

#### Options (où prendre la décision d’autorisation)
**Option A — Autorisation basée DB à chaque requête**
- Le serveur charge le rôle/droits depuis la DB (source of truth).
- Avantage : changements de rôles immédiats. Inconvénient : plus de lectures.
- Impact (propagation des changements) : si un droit/rôle est modifié en DB, l’effet est **immédiat** sur les requêtes suivantes, car l’autorisation est décidée côté serveur (pas besoin d’attendre un refresh).

**Option B — Autorisation portée par le JWT (claims)**
- Le JWT contient `role`/`permissions`.
- Avantage : performance et simplicité. Inconvénient : risque de **staleness** (si rôle change, il faut gérer invalidation/rotation des tokens).
- Note : si l’access token est **court** (ex : ~15 minutes, typiquement `JWT_EXPIRATION_SECONDS=900`) et que le refresh émet un **nouvel access token** en recalculant les claims depuis la DB, alors un changement de rôle/permissions devient effectif **au prochain refresh** (délai maximum = durée de vie restante de l’access token).
- Si un effet **immédiat** est requis (ex : retrait d’un droit critique), il faut compléter par un mécanisme de révocation/contrôle côté serveur (ex : révoquer le refresh token et/ou vérifier un `tokenVersion`/`lastRoleChangeAt` côté DB à chaque requête).

#### Nuance (important)
- Dans l’état actuel du MVP, le backend ré‑hydrate déjà l’utilisateur à partir de la DB sur chaque requête authentifiée (via l’identifiant porté par le JWT). Cela rend l’option “droits en DB” naturellement compatible avec un effet immédiat.
- En revanche, si l’équipe choisit de **faire confiance uniquement** aux claims du JWT pour autoriser (sans re‑vérification DB), alors le changement de droits ne pourra être effectif qu’à la **ré‑émission** d’un token (refresh / reconnexion) ou via un mécanisme explicite d’invalidation.

#### Actions techniques (si rôles)
- Ajouter le champ rôle (ou table) côté user + endpoints admin si nécessaire.
- Côté Spring Security : injecter des `GrantedAuthority` dans l’`Authentication` et protéger les routes (`@PreAuthorize` / règles `authorizeHttpRequests`).
- Définir le cycle de vie des tokens si on met le rôle en claim (durée courte, refresh, mécanisme d’invalidation).

### 10.4 Modération et “fonctionnel conformité”

#### Options
**Option A — Minimal**
- Pas de signalement, pas de blocage : uniquement suppression par auteur.

**Option B — Modération de base**
- Signalement (report), masquage, suppression par modérateur/admin.
- Journalisation des actions de modération.

#### Actions
- Définir le workflow (report -> review -> action) et la traçabilité (qui, quand, pourquoi).
- Définir une politique de rétention (combien de temps conserver les logs / historiques) selon besoin interne et contraintes légales applicables.

---

## 11) Temps réel & alertes (réseau social)

### Décisions à prendre
1) Les contenus doivent‑ils se mettre à jour **en temps réel** quand l’utilisateur est connecté (ex : nouvel article dans un topic suivi) ?
2) Si l’utilisateur n’est pas connecté, souhaite‑t‑on des **alertes** (ex : email) ? À quel rythme ?
3) Le même mécanisme s’applique‑t‑il à la création de **nouveaux topics** (découverte) ?

### 11.1 Mise à jour en temps réel (utilisateur connecté)

### Position (préconisation)
- En l’absence de fonctionnalités conversationnelles (type chat, présence, typing), privilégier **le polling** plutôt que WebSocket.
- Un polling toutes les **60 secondes** est généralement suffisant pour donner une “illusion” de temps réel côté utilisateur, avec un coût/risque d’exploitation bien plus faible.

#### Options
**Option A — Pas de temps réel (polling / refresh manuel)**
- L’UI se met à jour sur action utilisateur (rechargement) ou via polling (ex : toutes les 30–60s).

**Option B — Temps réel via WebSocket / SSE**
- Le serveur pousse des événements (ex : `article.created`, `topic.created`).

#### Impacts
- Option A : simple à exploiter et à sécuriser, mais moins “social network feel”.
- Option B : UX plus dynamique, mais ajoute une surface technique (connexion persistante, scalabilité, monitoring, auth sur canal temps réel).

#### Actions techniques
- Définir les événements minimum (ex : `article.created` avec `topicId`, `createdAt`).
- Choisir le protocole :
  - SSE : plus simple en “server -> client” uniquement.
  - WebSocket : bidirectionnel, plus flexible si chat/typing/etc.
- AuthN/AuthZ :
  - décider comment authentifier le canal (cookies existants + vérification côté serveur).
  - filtrer les événements selon abonnements (ne pousser que les topics suivis).
- Définir une stratégie multi‑instances (si scalabilité) : broker pub/sub (Redis, etc.) si besoin.

### 11.2 Alertes “offline” (utilisateur non connecté)

### Pré‑requis (fonctionnel)
- Mettre en place des alertes email implique d’avoir une **validation d’email** (compte réel) afin de limiter les envois vers des adresses non maîtrisées, réduire les erreurs (bounces) et améliorer la délivrabilité (voir §5).

### Position (préconisation)
- Les utilisateurs étant déjà très sollicités par email, éviter l’envoi **à chaque création** (peu recommandé).
- Si email retenu : privilégier un **récapitulatif individuel** déclenché par **inactivité** (ex : aucune connexion depuis 72h), plutôt qu’un digest systématique toutes les 24h.

#### Options
**Option A — Pas d’alertes offline**
- L’utilisateur découvre à la prochaine connexion.

**Option B — Alertes email (digest)**
- Email périodique (ex : quotidien/hebdo) récapitulant les nouveautés.

**Option C — Alertes email (quasi temps réel)**
- Email envoyé à chaque événement (ou à une fréquence élevée).

#### Impacts
- Digest : moins intrusif, moins coûteux, plus robuste (recommandé si email).
- Quasi temps réel : risque de spam/perception négative, plus de complexité (réputation email, rate limits, gestion désabonnement).

#### Actions techniques
- Ajouter des préférences de notification :
  - opt‑in/opt‑out, fréquence (none/daily/weekly), périmètre (topics suivis, nouveautés globales).
- Implémenter une file/asynchronisme pour l’envoi email (job scheduler / queue).
- Exiger un mécanisme de désinscription (unsub) et journaliser les envois.

### 11.3 Nouveaux topics : découverte et notifications

#### Options
**Option A — Découverte in‑app uniquement**
- Page “Explorer” / “Nouveaux topics”.

**Option B — Notifications (temps réel / digest)**
- Même mécanique que les nouveaux articles, mais scope “global”.

#### Actions
- Définir la règle produit : tous les utilisateurs vs seulement ceux qui ont opt‑in.
- Définir les limites (ex : pas plus de N notifications / période).

---

## 12) Liste de décisions à trancher (résumé)

1. Profils Spring : pilotage par env/args (dev/ci/staging/prod) ?
2. Déploiement : même domaine vs domaines séparés (impact CORS/cookies) ?
3. Cookies : `SameSite` et `Secure` selon la topologie + HTTPS.
4. Multi‑clients : cookies only vs hybride bearer.
5. Vérification email : OTP vs lien.
6. Reset password : OTP vs lien.
7. CI/CD : secrets/vars centralisés vs inputs manuels.
8. Observabilité : niveau requis.
9. Hardening auth : minimal vs renforcé.
10. Règles contenu : modifier/supprimer articles/commentaires (soft/hard delete, historique) ?
11. Topics : immuables vs archivables vs supprimables (et stratégie d’impact) ?
12. Droits : ownership simple vs rôles (modération/admin) ?
13. Autorisation : rôle/permissions en DB vs dans le JWT (gestion de staleness) ?
14. Modération : aucune vs signalement + actions + journalisation ?
15. Temps réel : pas de temps réel vs SSE/WebSocket (événements, auth, filtrage) ?
16. Alertes offline : aucune vs email digest vs quasi temps réel (préférences, unsub, anti‑spam) ?
17. Nouveaux topics : découverte in‑app vs notifications (scope et limites) ?
