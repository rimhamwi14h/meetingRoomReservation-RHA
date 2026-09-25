<div align="center">

# 🏫 Meeting Room Reservation API

### API REST de gestion et de réservation de salles universitaires

**Java 21 · Spring Boot · JPA · H2 · Flyway · JUnit · Checkstyle · GitHub Actions**

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white)
![Tests](https://img.shields.io/badge/Tests-21%20passing-brightgreen)
![Checkstyle](https://img.shields.io/badge/Checkstyle-0%20violations-brightgreen)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

</div>

---

## 📌 À propos du projet

Cette application backend permet de gérer les **bâtiments**, **salles**, **équipements**, **organisateurs** et **réservations** d'une université.

Deux modes de réservation sont disponibles :

- **Réservation manuelle** : le client choisit directement une salle.
- **Réservation automatique** : l'application choisit la salle la plus adaptée selon la disponibilité, la capacité, les équipements, la localisation de l'organisateur et les conflits de réservation.

> 📄 Le contrat HTTP complet de l'application est défini dans [`openapi.yml`](openapi.yml).

---

## 🧭 Sommaire

- [Fonctionnalités](#-fonctionnalités)
- [Technologies](#-technologies)
- [Architecture](#-architecture)
- [Modèle métier](#-modèle-métier)
- [Règles de réservation](#-règles-de-réservation)
- [Attribution automatique](#-attribution-automatique)
- [Endpoints REST](#-endpoints-rest)
- [Installation et lancement](#-installation-et-lancement)
- [Tests et qualité](#-tests-et-qualité)
- [Gestion des erreurs](#-gestion-des-erreurs)
- [Base de données](#-base-de-données)
- [Intégration continue](#-intégration-continue)
- [Documentation OpenAPI](#-documentation-openapi)

---

## ✨ Fonctionnalités

L'API permet de :

- créer, consulter et modifier des **bâtiments** ;
- créer, consulter et modifier des **salles** ;
- gérer le statut `AVAILABLE` / `MAINTENANCE` d'une salle ;
- gérer les **équipements** associés à une salle ;
- créer et consulter les **organisateurs** ;
- rechercher les salles disponibles sur une période donnée ;
- créer une réservation sur une salle précise ;
- attribuer automatiquement la meilleure salle compatible ;
- détecter et refuser les conflits de réservation ;
- annuler une réservation ;
- filtrer les réservations ;
- retourner des erreurs HTTP structurées et cohérentes.

---

## 🛠 Technologies

| Technologie | Rôle |
|---|---|
| **Java 21** | Langage principal |
| **Spring Boot 4.1.1** | Framework backend |
| **Spring Web MVC** | API REST |
| **Spring Data JPA** | Accès aux données |
| **Hibernate** | Implémentation JPA |
| **H2 Database** | Base de données en mémoire |
| **Flyway** | Migration du schéma SQL |
| **Jakarta Validation** | Validation des requêtes |
| **Lombok** | Réduction du code répétitif |
| **JUnit 5** | Tests |
| **Mockito** | Tests unitaires avec mocks |
| **Maven Wrapper** | Build et dépendances |
| **Checkstyle** | Lint Java |
| **GitHub Actions** | Intégration continue |

---

## 🧱 Architecture

```text
meetingRoomReservation-RHA
│
├── .github
│   └── workflows
│       └── ci.yml
│
├── config
│   └── checkstyle
│       └── checkstyle.xml
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── roomreservation
│   │   │       ├── controller
│   │   │       ├── exception
│   │   │       ├── model
│   │   │       ├── repository
│   │   │       ├── request
│   │   │       ├── response
│   │   │       └── service
│   │   └── resources
│   │       └── db
│   │           └── migration
│   │
│   └── test
│       └── java
│           └── roomreservation
│               ├── integration
│               └── service
│
├── mvnw
├── mvnw.cmd
├── openapi.yml
├── pom.xml
└── README.md
```

### Rôle des packages

| Package | Responsabilité |
|---|---|
| `controller` | Reçoit les requêtes HTTP et retourne les réponses |
| `service` | Contient les règles métier |
| `repository` | Accès aux données avec Spring Data JPA |
| `model` | Entités JPA |
| `request` | DTO reçus par l'API |
| `response` | DTO retournés par l'API |
| `exception` | Exceptions métier et gestion globale des erreurs |

---

## 🧩 Modèle métier

### 🏢 Building

Un bâtiment possède :

```text
id
name
numberOfFloors
```

Règles principales :

- `name` est unique sans tenir compte de la casse ;
- `numberOfFloors` est compris entre `1` et `200` ;
- les étages sont indexés de `0` à `numberOfFloors - 1`.

Exemple : pour `numberOfFloors = 5`, les étages valides sont `0, 1, 2, 3, 4`.

### 🚪 Room

Une salle possède :

```text
id
name
building
floor
capacity
status
equipment
```

Le statut peut être :

```text
AVAILABLE
MAINTENANCE
```

Une nouvelle salle est `AVAILABLE` par défaut.

### 🎥 Equipment

Un équipement possède :

```text
id
code
label
```

Le code est unique et respecte :

```text
^[A-Z][A-Z0-9_]{1,49}$
```

Exemples valides :

```text
PROJECTOR
WHITEBOARD
VIDEO_CONFERENCE
SCREEN_4K
```

### 👤 Organizer

Un organisateur possède :

```text
id
name
email
building
floor
```

L'adresse e-mail doit être valide et unique.

### 📅 Reservation

Une réservation contient :

```text
id
title
status
room
organizer
start
end
numberOfParticipants
requiredEquipmentCodes
createdAt
```

Statuts possibles :

```text
CONFIRMED
CANCELLED
```

Une réservation `CANCELLED` ne bloque plus la salle.

---

## 📏 Règles de réservation

### Période valide

Une réservation doit respecter :

```text
start < end
```

Elle doit également :

- commencer dans le futur ;
- durer au maximum **8 heures** ;
- utiliser des dates au format ISO 8601 avec fuseau ou décalage UTC.

Exemple :

```text
2026-10-15T14:00:00+02:00
```

### Détection des conflits

Deux réservations confirmées se chevauchent lorsque :

```text
existing.start < new.end
AND
existing.end > new.start
```

Exemple de conflit :

```text
Réservation existante : 10:00 - 12:00
Nouvelle réservation  : 11:00 - 13:00
```

Les réservations consécutives sont autorisées :

```text
10:00 - 11:00
11:00 - 12:00
```

---

## 🎯 Attribution automatique

Endpoint :

```http
POST /api/reservations/automatic
```

Le client ne fournit pas de `roomId`.

### 1. Filtrage des salles

Une salle candidate doit :

- avoir le statut `AVAILABLE` ;
- avoir une capacité suffisante ;
- posséder tous les équipements demandés ;
- ne pas avoir de réservation confirmée en conflit.

### 2. Calcul de la capacité inutilisée

```text
unusedCapacity = room.capacity - numberOfParticipants
```

### 3. Calcul de la distance

Même bâtiment :

```text
distance = abs(room.floor - organizer.floor)
```

Bâtiments différents :

```text
distance = 10 + abs(room.floor - organizer.floor)
```

### 4. Calcul du score

```text
score = distance * 10 + unusedCapacity
```

La salle ayant le **score le plus faible** est choisie.

### 5. Gestion des égalités

En cas d'égalité :

```text
1. nom de salle, sans tenir compte de la casse
2. identifiant
```

### Exemple

Organisateur :

```text
Building A
floor = 2
```

| Salle | Étage | Capacité | Participants | Distance | Places inutilisées | Score |
|---|---:|---:|---:|---:|---:|---:|
| Room Alpha | 2 | 25 | 20 | 0 | 5 | **5** |
| Room Beta | 4 | 20 | 20 | 2 | 0 | **20** |

➡️ `Room Alpha` est sélectionnée car `5 < 20`.

---

## 🔎 Recherche des salles disponibles

Endpoint :

```http
GET /api/rooms/available
```

Paramètres obligatoires :

```text
start
end
capacity
```

Paramètre optionnel :

```text
equipment
```

Exemple :

```text
GET /api/rooms/available
    ?start=2026-10-15T14:00:00+02:00
    &end=2026-10-15T16:00:00+02:00
    &capacity=20
    &equipment=PROJECTOR
```

Les salles sont filtrées selon leur statut, capacité, équipement et disponibilité.

Le tri est effectué par :

```text
1. unusedCapacity croissant
2. nom sans tenir compte de la casse
3. identifiant
```

Si aucune salle n'est compatible :

```json
[]
```

---

## 🌐 Endpoints REST

### Buildings

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/buildings` | Créer un bâtiment |
| `GET` | `/api/buildings` | Lister les bâtiments |
| `GET` | `/api/buildings/{buildingId}` | Consulter un bâtiment |
| `PUT` | `/api/buildings/{buildingId}` | Modifier un bâtiment |

### Rooms

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/rooms` | Créer une salle |
| `GET` | `/api/rooms` | Lister les salles |
| `GET` | `/api/rooms/{roomId}` | Consulter une salle |
| `PUT` | `/api/rooms/{roomId}` | Modifier une salle |
| `GET` | `/api/rooms/available` | Rechercher les salles disponibles |
| `PATCH` | `/api/rooms/{roomId}/status` | Modifier le statut |
| `PUT` | `/api/rooms/{roomId}/equipment` | Remplacer les équipements |

### Equipment

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/equipment` | Créer un équipement |
| `GET` | `/api/equipment` | Lister les équipements |

### Organizers

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/organizers` | Créer un organisateur |
| `GET` | `/api/organizers` | Lister les organisateurs |
| `GET` | `/api/organizers/{organizerId}` | Consulter un organisateur |

### Reservations

| Méthode | Endpoint | Description |
|---|---|---|
| `POST` | `/api/reservations` | Réserver une salle précise |
| `POST` | `/api/reservations/automatic` | Attribuer automatiquement une salle |
| `GET` | `/api/reservations` | Lister et filtrer les réservations |
| `GET` | `/api/reservations/{reservationId}` | Consulter une réservation |
| `PATCH` | `/api/reservations/{reservationId}/cancel` | Annuler une réservation |

### Filtres de réservation

`GET /api/reservations` accepte :

```text
roomId
organizerId
from
to
```

Si `from` et `to` sont fournis :

```text
from < to
```

doit être respecté.

---

## 🚀 Installation et lancement

### Prérequis

- **Java 21**
- **Git**

Maven n'a pas besoin d'être installé globalement : le projet utilise le **Maven Wrapper**.

### Cloner le projet

```bash
git clone <URL_DU_REPOSITORY>
cd meetingRoomReservation-RHA
```

### Lancer sous Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Lancer sous Linux / macOS

```bash
./mvnw spring-boot:run
```

Par défaut, le contrat OpenAPI utilise :

```text
http://localhost:8080
```

Si un autre port est configuré localement dans `application.properties`, utiliser ce port.

---

## 🧪 Tests et qualité

### Lancer les tests

Windows :

```powershell
.\mvnw.cmd test
```

Linux / macOS :

```bash
./mvnw test
```

État actuel :

```text
Tests run: 21
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### Lancer Checkstyle

Windows :

```powershell
.\mvnw.cmd checkstyle:check
```

Linux / macOS :

```bash
./mvnw checkstyle:check
```

État actuel :

```text
0 Checkstyle violations
BUILD SUCCESS
```

### Cas testés

Les tests couvrent notamment :

- capacité exactement suffisante ;
- salle trop petite ;
- salle en maintenance ;
- salle déjà réservée ;
- équipement manquant ;
- réservations consécutives ;
- distance même bâtiment / bâtiments différents ;
- calcul du score ;
- choix du meilleur score ;
- égalité départagée par nom puis identifiant ;
- absence de salle compatible ;
- création et consultation d'une salle ;
- attribution automatique ;
- disponibilité et ordre des résultats ;
- refus d'un conflit ;
- annulation puis nouvelle réservation ;
- format des réponses d'erreur.

Les tests utilisent la structure **GIVEN / WHEN / THEN** lorsque cela est pertinent.

---

## ⚠️ Gestion des erreurs

Les erreurs utilisent un format commun :

```json
{
  "code": "RESERVATION_NOT_FOUND",
  "message": "Reservation 999 not found",
  "timestamp": "2026-10-01T13:45:12Z",
  "path": "/api/reservations/999",
  "details": {
    "reservationId": 999
  },
  "fieldErrors": {}
}
```

### Principaux codes

| Code | HTTP | Signification |
|---|---:|---|
| `VALIDATION_ERROR` | 400 | Donnée invalide |
| `INVALID_RESERVATION_PERIOD` | 400 | Période invalide |
| `BUILDING_NOT_FOUND` | 404 | Bâtiment inexistant |
| `ROOM_NOT_FOUND` | 404 | Salle inexistante |
| `ORGANIZER_NOT_FOUND` | 404 | Organisateur inexistant |
| `RESERVATION_NOT_FOUND` | 404 | Réservation inexistante |
| `EQUIPMENT_NOT_FOUND` | 404 | Équipement inexistant |
| `ROOM_CAPACITY_EXCEEDED` | 409 | Capacité insuffisante |
| `MISSING_REQUIRED_EQUIPMENT` | 409 | Équipement manquant |
| `ROOM_ALREADY_RESERVED` | 409 | Conflit de réservation |
| `ROOM_UNAVAILABLE` | 409 | Salle indisponible |
| `NO_COMPATIBLE_ROOM` | 409 | Aucune salle compatible |
| `RESERVATION_ALREADY_CANCELLED` | 409 | Réservation déjà annulée |
| `RESOURCE_ALREADY_EXISTS` | 409 | Ressource déjà existante |
| `BUILDING_FLOOR_COUNT_CONFLICT` | 409 | Réduction d'étages impossible |

---

## 🗄 Base de données

Le projet utilise une base **H2 en mémoire**.

Cela signifie que les données sont réinitialisées lorsque l'application est arrêtée puis redémarrée.

Le schéma est géré avec **Flyway** :

```text
src/main/resources/db/migration
```

Les principales tables concernent :

```text
buildings
rooms
equipment
room_equipment
organizers
reservations
reservation_required_equipment
```

Pour un test manuel après redémarrage, l'ordre logique de création est :

```text
Building
   ↓
Equipment
   ↓
Room
   ↓
Organizer
   ↓
Reservation
```

---

## 🔄 Intégration continue

Le workflow GitHub Actions est défini dans :

```text
.github/workflows/ci.yml
```

Il s'exécute automatiquement sur la branche `main`, lors des `push` et `pull_request`.

Deux jobs sont exécutés :

| Job | Commande | Rôle |
|---|---|---|
| **Lint** | `./mvnw checkstyle:check` | Vérification Checkstyle |
| **Tests** | `./mvnw test` | Exécution des tests Maven |

Les deux jobs doivent être verts avant livraison.

---

## ✅ Validation des données

Jakarta Validation est utilisée sur les DTO de requête avec notamment :

```text
@NotNull
@NotBlank
@Min
@Max
@Positive
@Size
@Email
@Pattern
```

Exemple pour les codes d'équipement :

```text
^[A-Z][A-Z0-9_]{1,49}$
```

Une erreur de validation retourne :

```text
HTTP 400
VALIDATION_ERROR
```

Les erreurs de champs sont détaillées dans `fieldErrors`.

---

## 📡 Statuts HTTP principaux

| Statut | Utilisation |
|---|---|
| `200 OK` | Lecture ou modification réussie |
| `201 Created` | Ressource créée |
| `400 Bad Request` | Requête invalide |
| `404 Not Found` | Ressource inexistante |
| `409 Conflict` | Conflit métier |

Les créations retournent également un header :

```text
Location
```

---

## 📚 Documentation OpenAPI

Le fichier [`openapi.yml`](openapi.yml) décrit :

- les endpoints ;
- les paramètres ;
- les request bodies ;
- les réponses ;
- les codes HTTP ;
- les contraintes ;
- les objets JSON ;
- les erreurs possibles.

> En cas de doute sur le contrat HTTP, `openapi.yml` constitue la référence du projet.

---

## 📝 Résumé des règles métier

```text
✓ Une salle en maintenance ne peut pas être réservée.
✓ Une salle doit avoir une capacité suffisante.
✓ Tous les équipements demandés doivent être présents.
✓ Deux réservations CONFIRMED ne peuvent pas se chevaucher.
✓ Deux réservations consécutives sont autorisées.
✓ Une réservation CANCELLED ne bloque plus la salle.
✓ Une réservation dure au maximum 8 heures.
✓ L'attribution automatique choisit le score minimal.
✓ En cas d'égalité : nom alphabétique, puis identifiant.
```

---

<div align="center">

### 🎓 Projet Java avancé — ISMIN

API de réservation de salles universitaires développée avec Spring Boot.

</div>
