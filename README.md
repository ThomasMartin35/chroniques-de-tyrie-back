# Chroniques de Tyrie - Backend

Backend de l'application **Chroniques de Tyrie**, un site communautaire dédié à Guild Wars proposant des actualités, chroniques, guides et podcasts.

Le projet est développé en **Java 21** avec **Spring Boot** et expose une API REST consommée par une application React.

## Fonctionnalités

### Authentification & Sécurité

- Création de compte
- Connexion JWT
- Consultation de l'utilisateur connecté
- Validation des données
- Hash BCrypt des mots de passe
- Gestion centralisée des erreurs
- Spring Security
- Routes protégées
- Authentification stateless

## Stack technique

| Technologie | Rôle |
|-------------|------|
| Spring Boot | Framework principal |
| Spring Security + JWT | Authentification stateless |
| PostgreSQL + Flyway | Base de données + migrations |
| MapStruct + Lombok | Mapping & boilerplate |
| JUnit 5 + Mockito | Tests |

## Prérequis

- Java 21
- PostgreSQL
- Maven 3.9+

## Lancer le projet

```bash
# Cloner le repo
git clone https://github.com/ThomasMartin35/chroniques-de-tyrie-back.git

# Puis 
cd chroniques-de-tyrie-back

#Et 
mvn clean install

# Lancer avec Maven
mvn spring-boot:run
```

## Auteur

Thomas Martin - TyClick 

Projet personnel développé dans le cadre de la création de "Chroniques de Tyrie".

TyClick : [Lien vers TyClick](https://tyclick.fr/)

GitHub : [Lien vers mon GitHub](https://github.com/ThomasMartin35)

LinkedIn : [Thomas Martin](https://www.linkedin.com/in/thomas-martin35/)