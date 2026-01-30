# ORION – Spécifications fonctionnelles

**Projet MDD**  
Auteur : Orlando Espinoza  
Version : 0.0.1

## Objet du document
Ce document liste les fonctionnalités à implémenter pour le projet MDD, exprimées du point de vue métier (actions utilisateur).

## Périmètre
- Concerne uniquement la version MVP (Minimum Viable Product).
- Pas de back-office (pas de zone administrateur).

## Glossaire
| Terme français | Terme anglais | Description |
|---|---|---|
| Utilisateur | User | Personne physique connectée au réseau social |
| Sujet, thème | Subject, topic | Thème du monde de la programmation informatique |
| Article | Post | Message abordant un thème identifié |
| Abonnement | Subscription | Un utilisateur souhaite voir les articles d’un sujet |
| Fil | Feed | Ensemble des articles des abonnements d’un utilisateur |

## Liste des fonctionnalités

### Gestion des utilisateurs
- Accéder au formulaire de connexion et d’inscription à partir de la page d’accueil (non connectée).
- S’inscrire grâce à un e-mail, un mot de passe et un nom d’utilisateur.
- Se connecter à partir d’un e-mail ou d’un nom d’utilisateur et d’un mot de passe.
  - Attention : la connexion d’un utilisateur doit persister entre les sessions.
- Consulter son profil (e-mail, nom d’utilisateur et abonnements) via la page de profil.
- Modifier son profil (e-mail, nom d’utilisateur et mot de passe) via la page de profil.
- Se déconnecter.

### Gestion des abonnements
- Consulter la liste de tous les thèmes (que l’utilisateur y soit abonné ou non) via une page dédiée.
- S’abonner à un thème via la page des thèmes.
- Se désabonner via la page de profil.

### Gestion des articles
- Consulter son fil d’actualité sur la page d’accueil par chronologie (du plus récent au plus ancien) une fois connecté.
- Trier le fil d’actualité du plus récent au plus ancien ou bien du plus ancien au plus récent.
- Ajouter un article (choisir le thème associé, définir le titre et le contenu).
- Consulter un article (thème associé, titre, auteur, date, contenu, commentaires).
- Ajouter un commentaire à un article (définir le contenu).

## Exigences particulières
> Ces exigences concernent les fonctionnalités précédemment citées, et n’amènent pas l’ajout d’autres fonctionnalités.

- L’application doit être utilisable sur mobile et ordinateur (responsive design).
- Un mot de passe est valide si :
  - il comporte au moins 8 caractères
  - il contient au moins : un chiffre, une lettre minuscule, une lettre majuscule, un caractère spécial
- Lors de l’ajout d’un article, l’auteur et la date sont définis automatiquement.
- Lors de l’ajout d’un commentaire, l’auteur et la date sont définis automatiquement.
- Un commentaire correspond uniquement à un article (pas de sous-commentaires).
- Après avoir cliqué sur “S’abonner” dans la page des Thèmes, le bouton devient inactif et le texte “S’abonner” est remplacé par “Déjà abonné”.