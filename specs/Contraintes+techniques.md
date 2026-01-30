
# ORION – Contraintes techniques

**Projet MDD**  
Auteur : Orlando Espinoza  
Version : 0.0.1

## Objet du document
Ce document liste les contraintes techniques imposées par ORION pour le projet MDD (Monde de Dév).
Les choix laissés à la discrétion du développeur ne doivent pas entrer en conflit avec ces contraintes.

## Périmètre
- Concerne la version MVP (Minimum Viable Product) du projet MDD.
- Si le MVP atteint ses objectifs, les choix techniques seront conservés pour les versions suivantes.

## Architecture logicielle
- Le back-end est distinct du front-end. Une API permet l’interaction entre les deux.
- L’interaction entre le front-end et le back-end doit être sécurisée (méthode au choix du développeur).
- Respect des principes SOLID.

## Langages de programmation et frameworks
- **Back-end** : Java / Spring
	- Spring Core est obligatoire (IoC, DI).
	- Spring Boot est fortement recommandé.
	- Privilégier les modules Spring (ex : Spring Data) pour la base de données.
- **Front-end** : TypeScript / Angular
	- Respecter les bonnes pratiques Angular ([Angular Security Guide](https://angular.io/guide/security)).
	- Angular CLI est fortement recommandé.

## Gestion de code
- Utiliser Git et GitHub pour la gestion de code.
- Garder un seul repository pour tout le projet.