# Dictionnaire de données & mapping (DTO ↔ tables)

Objectif : compléter l’ERD avec une description **champ par champ** des données, leurs **contraintes**, les **règles de validation** et le **mapping DTO/API ↔ entités ↔ tables**.

## Sources (références du projet)

- Schéma SQL : `back/src/main/resources/schema.sql`
- Entités JPA : `back/src/main/java/com/openclassrooms/mddapi/entity/*`
- DTO (payloads API) : `back/src/main/java/com/openclassrooms/mddapi/dto/**`
- Règles métier (services) : `back/src/main/java/com/openclassrooms/mddapi/service/**` et `back/src/main/java/com/openclassrooms/mddapi/security/RefreshTokenService.java`
- Endpoints : `back/src/main/java/com/openclassrooms/mddapi/ApiEndpoints.java`

## Conventions

- **Table** : structure persistée (MySQL)
- **Entité** : représentation JPA/Hibernate de la table
- **DTO** : objets échangés via l’API (request/response)
- Les dates sont exprimées en `Instant` côté Java (UTC)

## Modèle relationnel (tables)

### 1) `mdd_user` — utilisateurs

| Colonne | Type (SQL) | Null | Unique | Description | Mapping principal |
|---|---:|:---:|:---:|---|---|
| `id` | `BIGINT` (AI, PK) | ✗ | ✓ | Identifiant utilisateur | `MddUserEntity.id` ↔ `UserDto.id` |
| `username` | `VARCHAR(100)` | ✗ | ✓ | Nom d’utilisateur | `MddUserEntity.username` ↔ `UserDto.username` ↔ `RegisterRequest.username` |
| `email` | `VARCHAR(100)` | ✗ | ✓ | Email (login possible via email ou username) | `MddUserEntity.email` ↔ `UserDto.email` ↔ `RegisterRequest.email` / `LoginRequest.email` |
| `password` | `VARCHAR(100)` | ✗ | ✗ | Mot de passe **hashé** (jamais renvoyé) | `MddUserEntity.password` ← `RegisterRequest.password` / `UserDto.password` (update) |
| `created_at` | `TIMESTAMP` (DEFAULT now) | ✓* | ✗ | Date de création | `MddUserEntity.createdAt` ↔ `UserDto.createdAt` |

Notes :
- Le DTO `UserDto.password` existe pour compatibilité front, mais les réponses API renvoient une chaîne vide (`""`).
- La validation de complexité de mot de passe est appliquée :
  - à l’inscription via `RegisterRequest.password` (regex)
  - à la mise à jour profil via `MddUserService` (regex côté service)

### 2) `topic` — thèmes

| Colonne | Type (SQL) | Null | Unique | Description | Mapping principal |
|---|---:|:---:|:---:|---|---|
| `id` | `BIGINT` (AI, PK) | ✗ | ✓ | Identifiant thème | `TopicEntity.id` ↔ `TopicDto.id` |
| `name` | `VARCHAR(100)` | ✗ | ✓ | Nom du thème | `TopicEntity.name` ↔ `TopicDto.name` |
| `description` | `VARCHAR(1000)` | ✓ | ✗ | Description | `TopicEntity.description` ↔ `TopicDto.description` |

### 3) `user_topic_subscription` — abonnements utilisateur↔thème

Clé primaire composite : (`user_id`, `topic_id`).

| Colonne | Type (SQL) | Null | PK/FK | Description | Mapping principal |
|---|---:|:---:|:---:|---|---|
| `user_id` | `BIGINT` | ✗ | PK + FK → `mdd_user.id` | Utilisateur | `UserTopicSubscriptionEntity.user` / `UserTopicSubscriptionId.userId` |
| `topic_id` | `BIGINT` | ✗ | PK + FK → `topic.id` | Thème | `UserTopicSubscriptionEntity.topic` / `UserTopicSubscriptionId.topicId` |
| `subscribed_at` | `TIMESTAMP` (DEFAULT now) | ✓* | - | Date d’abonnement | `UserTopicSubscriptionEntity.subscribedAt` |

DTO associé : `UserTopicSubscriptionDto` ne transporte que `topicId` (l’utilisateur vient de la session).

### 4) `article` — articles

| Colonne | Type (SQL) | Null | FK | Description | Mapping principal |
|---|---:|:---:|:---:|---|---|
| `id` | `BIGINT` (AI, PK) | ✗ | - | Identifiant article | `ArticleEntity.id` ↔ `ArticleDto.id` |
| `title` | `VARCHAR(100)` | ✗ | - | Titre | `ArticleEntity.title` ↔ `ArticleDto.title` ↔ `CreateArticleRequest.title` |
| `content` | `TEXT` | ✗ | - | Contenu | `ArticleEntity.content` ↔ `ArticleDto.content` ↔ `CreateArticleRequest.content` |
| `created_at` | `TIMESTAMP` (DEFAULT now) | ✓* | - | Date de création | `ArticleEntity.createdAt` ↔ `ArticleDto.createdAt` |
| `author_id` | `BIGINT` | ✗ | FK → `mdd_user.id` | Auteur | `ArticleEntity.author.id` ↔ `ArticleDto.authorId` (vient de la session) |
| `topic_id` | `BIGINT` | ✗ | FK → `topic.id` | Thème | `ArticleEntity.topic.id` ↔ `ArticleDto.topicId` ↔ `CreateArticleRequest.topicId` |

### 5) `article_comment` — commentaires

| Colonne | Type (SQL) | Null | FK | Description | Mapping principal |
|---|---:|:---:|:---:|---|---|
| `id` | `BIGINT` (AI, PK) | ✗ | - | Identifiant commentaire | `ArticleCommentEntity.id` ↔ `CommentDto.id` |
| `content` | `TEXT` | ✗ | - | Contenu | `ArticleCommentEntity.content` ↔ `CommentDto.content` |
| `created_at` | `TIMESTAMP` (DEFAULT now) | ✓* | - | Date de création | `ArticleCommentEntity.createdAt` ↔ `CommentDto.createdAt` |
| `author_id` | `BIGINT` | ✗ | FK → `mdd_user.id` | Auteur | `ArticleCommentEntity.author.id` ↔ `CommentDto.authorId` (vient de la session) |
| `article_id` | `BIGINT` | ✗ | FK → `article.id` | Article lié | `ArticleCommentEntity.article.id` ↔ `CommentDto.articleId` |

### 6) `refresh_token` — session (refresh token)

Ce stockage sert à rendre la session “persistante” (rotation du refresh token) tout en gardant un modèle sûr : **le token brut n’est jamais stocké**, seule son empreinte est persistée.

| Colonne | Type (SQL) | Null | Unique | Description | Mapping principal |
|---|---:|:---:|:---:|---|---|
| `id` | `BIGINT` (AI, PK) | ✗ | ✓ | Identifiant | `RefreshTokenEntity.id` |
| `user_id` | `BIGINT` | ✗ | ✓ | 1 refresh token actif par utilisateur | `RefreshTokenEntity.userId` |
| `token_hash` | `VARCHAR(255)` | ✗ | ✓ | Empreinte SHA-256 (Base64) du refresh token | `RefreshTokenEntity.tokenHash` |
| `expires_at` | `TIMESTAMP` | ✗ | ✗ | Expiration du refresh token | `RefreshTokenEntity.expiresAt` |
| `created_at` | `TIMESTAMP` | ✓* | ✗ | Création | `RefreshTokenEntity.createdAt` |
| `updated_at` | `TIMESTAMP` | ✓* | ✗ | Dernière mise à jour | `RefreshTokenEntity.updatedAt` |

## Règles de validation (DTO + services)

### Auth

- `RegisterRequest.username` : `@NotBlank`, `@Size(max=100)`
- `RegisterRequest.email` : `@NotBlank`, `@Email`, `@Size(max=254)`
- `RegisterRequest.password` : `@NotBlank` + regex complexité :

```regex
^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[#?!@$%^&*-]).{8,}$
```

- `LoginRequest.email` : `@NotBlank`, `@Size(max=254)` (peut être un email OU un username côté service)
- `LoginRequest.password` : `@NotBlank`, `@Size(max=72)`

### Articles

- `CreateArticleRequest.title` : `@NotBlank`, `@Size(max=100)`
- `CreateArticleRequest.content` : `@NotBlank`
- `CreateArticleRequest.topicId` : `@NotNull`, `@Positive`

### Commentaires

- `CommentDto.content` : `@NotBlank`, `@Size(max=2000)`
- `CommentDto.articleId` : `@NotNull`, `@Positive`

### Abonnements

- `UserTopicSubscriptionDto.topicId` : `@NotNull`, `@Positive`

### Profil (update)

- `PUT /api/users` : payload `UserDto`
- La mise à jour est **partielle** : seuls les champs non vides sont pris en compte.
- Si `password` est fourni, il doit respecter la même politique de complexité que l’inscription (validation côté service).
- `username` et `email` sont vérifiés en **unicité** (conflit `409`).

## Mapping “DTO ↔ tables” par endpoint

Endpoints (liste) : `back/src/main/java/com/openclassrooms/mddapi/ApiEndpoints.java`

### `POST /api/auth/register`

- Request : `RegisterRequest`
  - `username` → `mdd_user.username` (unique)
  - `email` → `mdd_user.email` (unique)
  - `password` → `mdd_user.password` (hashé via `PasswordEncoder`)
- Effets de bord : création/rotation d’un refresh token (`refresh_token`) + cookies (access/refresh/xsrf)
- Response : `AuthResponseDto`
  - `token` : access token JWT
  - `user` : `UserDto` (password vide)

### `POST /api/auth/login`

- Request : `LoginRequest`
  - `email` : peut contenir un email **ou** un username (supporté dans le service)
  - `password` : vérifié via `PasswordEncoder.matches`
- Effets de bord : rotation refresh token + cookies
- Response : `AuthResponseDto`

### `POST /api/auth/refresh`

- Pas de body : utilise le cookie refresh
- Rotation : `refresh_token.token_hash` (SHA-256 Base64) + `expires_at`
- Response : `AuthResponseDto` (nouvel access token)

### `GET /api/auth/me`

- Response : `UserDto` (basé sur l’utilisateur authentifié)

### `GET /api/topics` / `GET /api/topics/{id}`

- Response : `TopicDto` ↔ table `topic`

### `GET /api/subscriptions`

- Response : liste `UserTopicSubscriptionDto` (liste des `topicId` abonnés)
- Source : table `user_topic_subscription`

### `POST /api/subscriptions`

- Request : `UserTopicSubscriptionDto.topicId`
- Insert si absent :
  - `user_topic_subscription.user_id` = utilisateur authentifié
  - `user_topic_subscription.topic_id` = `topicId`
  - `subscribed_at` auto
- Response : liste mise à jour des abonnements

### `DELETE /api/subscriptions?topicId=...`

- Supprime la ligne correspondante dans `user_topic_subscription`
- Response : liste mise à jour des abonnements

### `GET /api/articles` / `GET /api/articles/{id}`

- Response : `ArticleDto` ↔ table `article`
- Particularité métier : `/api/articles` filtre les articles par thèmes auxquels l’utilisateur est abonné.

### `POST /api/articles`

- Request : `CreateArticleRequest`
  - `title` → `article.title`
  - `content` → `article.content`
  - `topicId` → `article.topic_id`
  - `author_id` vient de la session (utilisateur authentifié)
- `created_at` est automatique
- Response : `ArticleDto`

### `GET /api/comments?articleId=...`

- Response : liste `CommentDto` ↔ table `article_comment`

### `POST /api/comments`

- Request : `CommentDto` (utilisé comme payload de création)
  - champs requis : `content`, `articleId`
  - `authorId` est ignoré côté service (vient de la session)
  - `createdAt` est automatique
- Insert : `article_comment`
- Response : `CommentDto`

### `PUT /api/users`

- Request : `UserDto`
  - champs pris en compte (si non vides) : `username`, `email`, `password`
  - `password` → `mdd_user.password` (hashé)
- Response : `UserDto` (password vide)

