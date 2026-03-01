# FAQ utilisateur — Utilisation de l’application

Ce document regroupe :

1) Une FAQ orientée “utilisation” (non technique)
2) Un espace dédié aux captures d’écran à fournir dans le livrable

## Comment s’inscrire (créer un compte) ?

1. Depuis la page d’accueil, cliquez sur **S’inscrire**.
2. Renseignez **Nom d’utilisateur**, **Adresse e-mail** et **Mot de passe**.
3. Cliquez sur **S’inscrire**.

Après validation, vous êtes redirigé vers le fil **Articles**.

## Comment se connecter ?

1. Depuis la page d’accueil, cliquez sur **Se connecter**.
2. Renseignez votre **e-mail ou nom d’utilisateur**, puis votre **mot de passe**.
3. Validez.

## Où est le fil (liste des articles) ?

- Le fil est accessible via le bouton **Articles** dans l’en-tête.
- Si vous n’êtes pas connecté, l’application vous redirige vers la page d’accueil.

## Comment trier les articles ?

Sur la page **Articles**, utilisez la zone **Trier** (flèche ↑/↓) pour inverser l’ordre d’affichage (du plus récent au plus ancien, ou l’inverse).

## Comment s’abonner à un thème ?

1. Allez dans **Thèmes**.
2. Sur un thème, cliquez sur **s’abonner**.

Le bouton passe à **se désabonner** quand l’abonnement est actif.

## Où voir (et gérer) mes abonnements ?

- Allez dans **Profil**.
- La liste de vos thèmes abonnés s’affiche, avec un bouton **se désabonner** pour chacun.

## Comment mettre à jour mon profil ?

1. Allez dans **Profil**.
2. Modifiez les champs souhaités.
3. Cliquez sur **Sauvegarder**.

## Comment créer un article ?

1. Allez dans **Articles**.
2. Cliquez sur **Créer un article**.
3. Remplissez le formulaire, puis cliquez sur **Créer**.

## Comment commenter un article ?

1. Allez dans **Articles**.
2. Ouvrez un article en cliquant sur sa carte.
3. Dans la section **Commentaires**, saisissez votre message et cliquez sur **Sauvegarder**.

## Comment se déconnecter ?

- Cliquez sur **Se déconnecter** dans l’en-tête.

## Que se passe-t-il si ma session expire ?

- La session est dite “persistante” car l’application tente de vous reconnecter automatiquement via un **refresh token** stocké en cookie.
- Elle peut néanmoins expirer ou devenir invalide (durée de vie du refresh token, déconnexion, cookies supprimés, changement de secret côté serveur, etc.).
- Dans ce cas, l’application vous redirige vers la page d’accueil et il faudra vous reconnecter pour accéder aux pages protégées (**Articles**, **Thèmes**, **Profil**).

# Captures d’écran — espace livrable

Ce document est prévu pour centraliser les captures d’écran de l’application (livrable). Les images sont à déposer dans `docs/screenshots/`.

## Convention de nommage

- Format: `YYYY-MM-DD_<ecran>_<action>.png`
- Exemple: `2026-03-01_login_erreur-mdp.png`

## Liste des captures (à compléter)

| Écran | Objectif | Fichier (à déposer) |
|---|---|---|
| Accueil | Page d’accueil avant connexion | `docs/screenshots/accueil.png` |
| Inscription | Formulaire + validation OK | `docs/screenshots/inscription.png` |
| Connexion | Formulaire de connexion | `docs/screenshots/connexion.png` |
| Connexion (erreur) | Message en cas d’identifiants invalides | `docs/screenshots/connexion-erreur.png` |
| Articles | Fil d’articles (après connexion) | `docs/screenshots/articles.png` |
| Articles (tri) | Exemple de tri appliqué | `docs/screenshots/articles-tri.png` |
| Thèmes | Liste des thèmes | `docs/screenshots/themes.png` |
| Thèmes (abonnement) | Abonné / désabonné (un exemple) | `docs/screenshots/themes-abonnement.png` |
| Profil | Page profil (avant/après modification) | `docs/screenshots/profil.png` |
| Créer un article | Formulaire de création | `docs/screenshots/creer-article.png` |
| Détails article | Page détail + commentaires | `docs/screenshots/article-details.png` |
| Déconnexion | Retour à l’accueil après logout | `docs/screenshots/deconnexion.png` |

## Galerie (liens)

Ajoutez ici les images une fois déposées.

- ![Accueil](screenshots/accueil.png)
- ![Inscription](screenshots/inscription.png)
- ![Connexion](screenshots/connexion.png)
- ![Connexion — erreur](screenshots/connexion-erreur.png)
- ![Articles](screenshots/articles.png)
- ![Articles — tri](screenshots/articles-tri.png)
- ![Thèmes](screenshots/themes.png)
- ![Thèmes — abonnement](screenshots/themes-abonnement.png)
- ![Profil](screenshots/profil.png)
- ![Créer un article](screenshots/creer-article.png)
- ![Détails article](screenshots/article-details.png)
- ![Déconnexion](screenshots/deconnexion.png)
