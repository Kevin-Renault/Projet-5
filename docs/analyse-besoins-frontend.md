# Analyse des besoins front-end (synthèse)

Objectif : fournir une synthèse “**besoins → écrans → composants → endpoints API**” à partir des specs ORION et de l’implémentation réelle (Angular + Spring Boot).

## Sources

- Spécifications fonctionnelles : `specs/Spécifications+fonctionnelles.md`
- Contraintes techniques : `specs/Contraintes+techniques.md`
- Routes front : `front/src/app/app-routing.module.ts`
- Services front (API) :
  - Auth : `front/src/app/core/auth/auth.service.ts`
  - Users : `front/src/app/core/services/real/user.service.ts`
  - Topics : `front/src/app/core/services/real/topic.service.ts`
  - Subscriptions : `front/src/app/core/services/real/subscription.service.ts`
  - Articles : `front/src/app/core/services/real/article.service.ts`
  - Comments : `front/src/app/core/services/real/article-comment.service.ts`
- Endpoints back : `back/src/main/java/com/openclassrooms/mddapi/ApiEndpoints.java`

## Besoins (MVP) — reformulation

### Gestion des utilisateurs

- Accès à l’inscription et à la connexion depuis une page publique
- Inscription : username + email + mot de passe
- Connexion : email **ou** username + mot de passe
- Session persistante (auto-reconnexion via mécanisme de refresh)
- Consultation et modification du profil
- Déconnexion

### Gestion des abonnements

- Consulter la liste des thèmes (abonné / non abonné)
- S’abonner depuis la page Thèmes
- Se désabonner depuis le profil

### Gestion des articles

- Consulter le fil (articles des thèmes abonnés) par ordre chronologique
- Trier le fil (asc/desc)
- Créer un article (thème, titre, contenu)
- Consulter un article et ses commentaires
- Ajouter un commentaire

## Écrans (routes) → composants → appels API

> Convention : les pages privées sont protégées par `AuthGuard`.

### 1) Accueil (public) — `/`

- Besoin : page publique, accès à “S’inscrire” / “Se connecter”, et tentative d’auto-session.
- Composant : `HomeComponent` (`front/src/app/features/home/home.component.ts`)
- API utilisée :
  - `GET /api/auth/csrf` (initialisation CSRF)
  - `GET /api/auth/me` (récupération session)
  - `POST /api/auth/refresh` (fallback si `me` échoue)
- Comportement :
  - si session valide → redirection `/articles`
  - sinon reste sur `/` (message d’erreur possible)

### 2) Inscription — `/user/register`

- Besoin : créer un compte
- Composant : `RegisterComponent` (`front/src/app/features/user/register/register.component.ts`)
- API :
  - `GET /api/auth/csrf`
  - `POST /api/auth/register`
- Données envoyées : `username`, `email`, `password`
- Résultat : redirection `/articles` si OK

### 3) Connexion — `/user/login`

- Besoin : se connecter (email ou username)
- Composant : `LoginComponent` (`front/src/app/features/user/login/login.component.ts`)
- API :
  - `GET /api/auth/csrf`
  - `POST /api/auth/login`
- Données envoyées : `email` (contient email ou username), `password`
- Résultat : redirection `/articles` si OK

### 4) Fil d’articles — `/articles` (protégé)

- Besoin : voir le fil (articles des abonnements) + tri asc/desc
- Composant : `ArticleListComponent` (`front/src/app/features/article/article-list.component.ts`)
- Sous-composants réutilisés : `HeaderComponent` (navigation + logout)
- API :
  - `GET /api/articles` (liste filtrée côté back par abonnements)
  - `GET /api/users` (résolution authorId → username)
- Notes :
  - Tri géré côté front via `sortOrder` (asc/desc)

### 5) Créer un article — `/articles/create` (protégé)

- Besoin : ajouter un article (thème, titre, contenu). Auteur/date auto.
- Composant : `ArticleCreateComponent` (`front/src/app/features/article/article-create.component.ts`)
- API :
  - `GET /api/topics` (options de sélection)
  - `POST /api/articles` (création)

### 6) Détails article + commentaires — `/articles/:id/comment` (protégé)

- Besoin : consulter un article, voir ses commentaires, ajouter un commentaire. Auteur/date auto.
- Composant : `ArticleCommentComponent` (`front/src/app/features/article/article-comment.component.ts`)
- API :
  - `GET /api/articles/{id}`
  - `GET /api/comments?articleId={id}`
  - `POST /api/comments`
  - `GET /api/users/{id}` (nom auteur commentaire)
  - `GET /api/topics/{id}` (nom du thème)

### 7) Thèmes — `/topics` (protégé)

- Besoin : lister tous les thèmes, s’abonner / se désabonner depuis la page
- Composant : `TopicComponent` (`front/src/app/features/topic/topic.component.ts`)
- API :
  - `GET /api/topics`
  - `GET /api/subscriptions` (pour savoir si déjà abonné)
  - `POST /api/subscriptions` (s’abonner)
  - `DELETE /api/subscriptions?topicId=...` (se désabonner)
- Règle ORION : après abonnement, le bouton passe en “déjà abonné” (front : état `isSubscribed`)

### 8) Profil — `/user/profile` (protégé)

- Besoin : consulter/modifier ses infos + gérer ses abonnements (désabonner)
- Composant : `ProfileComponent` (`front/src/app/features/user/profile/profile.component.ts`)
- API :
  - `GET /api/topics` (liste globale)
  - `GET /api/subscriptions` (thèmes abonnés)
  - `DELETE /api/subscriptions?topicId=...` (désabonnement)
  - `PUT /api/users` (mise à jour profil)

### 9) Erreur / route inconnue — `**` (protégé)

- Besoin : fallback si route inconnue (seulement pour utilisateurs authentifiés)
- Composant : `ErrorComponent` (`front/src/app/shared/error/error.component`)

## Navigation globale (en-tête)

- Composant : `HeaderComponent` (`front/src/app/shared/header/header.component`)
- Besoins : accès rapide Articles / Thèmes / Profil + logout
- API logout :
  - `POST /api/auth/logout` (avec init CSRF côté `AuthService.logout()`)

## Contraintes ORION prises en compte

- Responsive : géré par styles front (non détaillé ici)
- Mot de passe : regex complexité côté front + validation côté back
- Auteur/date auto pour articles et commentaires : appliqué côté back (et le front ne doit pas pouvoir imposer un autre auteur)
- Commentaires “plats” (pas de sous-commentaires) : une table de commentaires liée à un article

## Tableau récapitulatif (écran → endpoints)

| Écran | Route | Endpoints |
|---|---|---|
| Accueil | `/` | `GET /api/auth/csrf`, `GET /api/auth/me`, `POST /api/auth/refresh` |
| Inscription | `/user/register` | `GET /api/auth/csrf`, `POST /api/auth/register` |
| Connexion | `/user/login` | `GET /api/auth/csrf`, `POST /api/auth/login` |
| Articles (fil) | `/articles` | `GET /api/articles`, `GET /api/users` |
| Créer article | `/articles/create` | `GET /api/topics`, `POST /api/articles` |
| Article + commentaires | `/articles/:id/comment` | `GET /api/articles/{id}`, `GET /api/comments?articleId=...`, `POST /api/comments`, `GET /api/users/{id}`, `GET /api/topics/{id}` |
| Thèmes | `/topics` | `GET /api/topics`, `GET /api/subscriptions`, `POST /api/subscriptions`, `DELETE /api/subscriptions?topicId=...` |
| Profil | `/user/profile` | `GET /api/topics`, `GET /api/subscriptions`, `DELETE /api/subscriptions?topicId=...`, `PUT /api/users` |

