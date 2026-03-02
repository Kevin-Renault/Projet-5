# 2.3 API et schémas de données

Cette section présente la conception de l’API REST (endpoints + méthodes HTTP), des exemples de requêtes/réponses JSON et la structure des données (entités, relations, contraintes).

## 1) Vue d’ensemble de l’API

- Base path : `/api`
- Format : JSON (`Content-Type: application/json`)
- Authentification : JWT **via cookie** (access + refresh) + endpoints de session sous `/api/auth/*`
- CSRF : token via cookie `XSRF-TOKEN` + header `X-XSRF-TOKEN` (pour les requêtes dites “unsafe”)
- Documentation OpenAPI :
  - JSON : `/v3/api-docs`
  - UI : `/swagger-ui/index.html`

## 2) Endpoints (liste + méthodes HTTP)

Sources de vérité (code) :
- Endpoints constants : `back/src/main/java/com/openclassrooms/mddapi/ApiEndpoints.java`
- Controllers : `back/src/main/java/com/openclassrooms/mddapi/controller/*`

| Domaine | Endpoint | Méthode | Auth | Body (request) | Réponse (200/201) |
|---|---|---:|:---:|---|---|
| Auth | `/api/auth/csrf` | GET | Non | — | 204 + cookies/header CSRF |
| Auth | `/api/auth/register` | POST | Non | `RegisterRequest` | `AuthResponseDto` |
| Auth | `/api/auth/login` | POST | Non | `LoginRequest` | `AuthResponseDto` |
| Auth | `/api/auth/refresh` | POST | Cookie refresh | — | `AuthResponseDto` |
| Auth | `/api/auth/logout` | POST | Oui | — | 204 + clear cookies |
| Auth | `/api/auth/me` | GET | Oui | — | `UserDto` |
| Users | `/api/users` | GET | Oui | — | `UserDto[]` |
| Users | `/api/users/{id}` | GET | Oui | — | `UserDto` |
| Users | `/api/users` | PUT | Oui | `UserDto` | `UserDto` |
| Topics | `/api/topics` | GET | Oui | — | `TopicDto[]` |
| Topics | `/api/topics/{id}` | GET | Oui | — | `TopicDto` |
| Subscriptions | `/api/subscriptions` | GET | Oui | — | `UserTopicSubscriptionDto[]` |
| Subscriptions | `/api/subscriptions` | POST | Oui | `UserTopicSubscriptionDto` | `UserTopicSubscriptionDto[]` |
| Subscriptions | `/api/subscriptions?topicId=…` | DELETE | Oui | — | `UserTopicSubscriptionDto[]` |
| Articles | `/api/articles` | GET | Oui | — | `ArticleDto[]` |
| Articles | `/api/articles/{id}` | GET | Oui | — | `ArticleDto` |
| Articles | `/api/articles` | POST | Oui | `CreateArticleRequest` | `ArticleDto` (201) |
| Comments | `/api/comments?articleId=…` | GET | Oui | — | `CommentDto[]` |
| Comments | `/api/comments` | POST | Oui | `CommentDto` (payload création) | `CommentDto` (201) |
| Utils | `/api/env` | GET | Non | — | `{ "env": "dev" }` |

Notes :
- Le login supporte **email OU username** dans `LoginRequest.email` (implémentation côté service).
- `PUT /api/users` met à jour l’utilisateur authentifié (pas de `{id}` dans l’URL).

## 3) Exemples JSON (requêtes / réponses)

### 3.1 Inscription — `POST /api/auth/register`

Request (`RegisterRequest`) :

```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "StrongP@ssw0rd"
}
```

Réponse 200 (`AuthResponseDto`) :

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "",
    "role": "user",
    "createdAt": "2024-01-01T12:00:00Z"
  }
}
```

### 3.2 Connexion — `POST /api/auth/login`

Request (`LoginRequest`) :

```json
{
  "email": "john.doe@example.com",
  "password": "StrongP@ssw0rd"
}
```

Réponse 200 : même forme que `AuthResponseDto`.

### 3.3 Rafraîchir le token — `POST /api/auth/refresh`

- Pas de body : le refresh token est lu depuis un cookie.
- Réponse 200 : `AuthResponseDto` (nouveau `token` + cookies mis à jour).

### 3.4 Utilisateur courant — `GET /api/auth/me`

Réponse 200 (`UserDto`) :

```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "",
  "role": "user",
  "createdAt": "2024-01-01T12:00:00Z"
}
```

### 3.5 Liste des topics — `GET /api/topics`

Réponse 200 (`TopicDto[]`) :

```json
[
  {
    "id": 1,
    "name": "Java",
    "description": "All things Java and the JVM ecosystem."
  }
]
```

### 3.6 S’abonner à un topic — `POST /api/subscriptions`

Request (`UserTopicSubscriptionDto`) :

```json
{
  "topicId": 1
}
```

Réponse 200 : liste des abonnements (topicId) :

```json
[
  { "topicId": 1 }
]
```

### 3.7 Créer un article — `POST /api/articles`

Request (`CreateArticleRequest`) :

```json
{
  "title": "Why I like Spring Boot",
  "content": "Spring Boot makes it easy to create production-ready apps...",
  "topicId": 1
}
```

Réponse 201 (`ArticleDto`) :

```json
{
  "id": 10,
  "title": "Why I like Spring Boot",
  "content": "Spring Boot makes it easy to create production-ready apps...",
  "createdAt": "2024-01-01T12:00:00Z",
  "authorId": 1,
  "topicId": 1
}
```

### 3.8 Lister les commentaires d’un article — `GET /api/comments?articleId=10`

Réponse 200 (`CommentDto[]`) :

```json
[
  {
    "id": 50,
    "content": "Great article, thanks for sharing!",
    "createdAt": "2024-01-02T09:30:00Z",
    "authorId": 1,
    "articleId": 10
  }
]
```

### 3.9 Erreurs — exemple de réponse JSON (`ApiErrorResponse`)

```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation error",
  "path": "/api/articles",
  "fieldErrors": {
    "title": "must not be blank",
    "topicId": "must be greater than 0"
  }
}
```

## 4) Schémas de données (entités, relations, contraintes)

### 4.1 Modèle relationnel (tables)

Le schéma SQL est défini dans `back/src/main/resources/schema.sql`.

- `mdd_user` : utilisateurs (username/email uniques)
- `topic` : thèmes (name unique)
- `user_topic_subscription` : jointure user↔topic (PK composite `(user_id, topic_id)`)
- `article` : articles (FK vers `mdd_user` et `topic`)
- `article_comment` : commentaires (FK vers `mdd_user` et `article`)
- `refresh_token` : refresh token persisté (1 actif par user, stockage par hash)

### 4.2 Diagramme des relations (ERD / entités)

Le diagramme ci-dessous représente les entités et leurs relations :

![Diagramme ERD](../specs/model.db.svg)

### 4.3 Contraintes notables

- Unicité :
  - `mdd_user.username`, `mdd_user.email`
  - `topic.name`
  - `refresh_token.user_id` (1 refresh token actif par utilisateur)
  - `refresh_token.token_hash`
- Intégrité référentielle : FKs avec `ON DELETE CASCADE` (`user_topic_subscription`, `article`, `article_comment`, `refresh_token`)
- Validation côté API (Jakarta Validation) :
  - password d’inscription : complexité via regex (maj/min/chiffre/symbole, min 8)
  - `CommentDto.content` max 2000
  - `CreateArticleRequest.title` max 100

## 5) Référence détaillée (champ par champ)

Pour le détail “colonne ↔ entité ↔ DTO” (et les règles de validation associées), voir :
- `docs/dictionnaire-donnees.md`
