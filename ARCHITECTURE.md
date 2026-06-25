# Chroniques de Tyrie — Architecture

##  Technology Stack

The projet uses:

- Java 21
- Spring Boot
- PostgreSQL
- Flyway
- React
- Spring Security
- Lombok

## General Backend Organization

The backend is organized by functional domain.

Example :

```text
auth
├── controller
├── service
└── dto
    ├── request
    └── response

user
├── entity
├── repository
└── dto

common
├── config
├── exception
└── dto
    └── response