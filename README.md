````
# Meeting Room Reservation API

API REST développée avec **Java et Spring Boot** permettant de gérer les salles d'une université et leurs réservations.

L'application permet de gérer les bâtiments, les salles, les équipements et les organisateurs, mais également de créer des réservations de deux manières :

- en choisissant directement une salle ;
- en laissant l'application sélectionner automatiquement la salle la plus adaptée.

La sélection automatique prend en compte la capacité de la salle, les équipements demandés, les conflits de réservation ainsi que la distance entre la salle et l'organisateur.

Le contrat HTTP complet de l'application est décrit dans le fichier :

```text
openapi.yml
```

---

# 1. Objectif du projet

Le projet répond au besoin d'une université souhaitant disposer d'un système de réservation de salles.

L'application doit notamment permettre :

- de créer et consulter des bâtiments ;
- de créer et modifier des salles ;
- de gérer les équipements disponibles dans les salles ;
- de créer et consulter des organisateurs ;
- de rechercher les salles disponibles sur une période donnée ;
- de réserver directement une salle précise ;
- d'attribuer automatiquement une salle adaptée ;
- d'empêcher les conflits de réservation ;
- d'annuler une réservation ;
- de consulter les réservations avec différents filtres ;
- de placer temporairement une salle en maintenance ;
- de retourner des erreurs HTTP structurées.

---

# 2. Technologies utilisées

Le projet utilise principalement :

| Technologie | Utilisation |
|---|---|
| Java 21 | Langage principal |
| Spring Boot 4.1.1 | Framework backend |
| Spring Web MVC | Création de l'API REST |
| Spring Data JPA | Accès aux données |
| Hibernate | Implémentation JPA |
| H2 Database | Base de données en mémoire |
| Flyway | Création et versionnement du schéma SQL |
| Jakarta Validation | Validation des requêtes |
| Lombok | Réduction du code répétitif |
| JUnit 5 | Tests unitaires et d'intégration |
| Mockito | Simulation des dépendances dans les tests unitaires |
| Maven | Gestion des dépendances et du build |
| Git | Gestion de versions |
| GitHub Actions | Intégration continue |

---

# 3. Architecture du projet

Le projet suit une architecture classique Spring Boot.

```text
meetingRoomReservation-RHA
│
├── .github
│   └── workflows
│       └── ci.yml
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
│   │   │
│   │   └── resources
│   │       ├── db
│   │       │   └── migration
│   │       └── application.properties
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

## Rôle des packages

### `controller`

Les controllers reçoivent les requêtes HTTP.

Ils utilisent les services pour exécuter les règles métier puis retournent les réponses HTTP.

Exemples :

```text
BuildingController
RoomController
EquipmentController
OrganizerController
ReservationController
```

### `service`

Les services contiennent la logique métier de l'application.

Par exemple :

```text
BuildingService
RoomService
EquipmentService
OrganizerService
ReservationService
```

`ReservationService` contient notamment la logique de création des réservations et d'attribution automatique des salles.

### `repository`

Les repositories permettent de communiquer avec la base de données grâce à Spring Data JPA.

Exemples :

```text
BuildingRepository
RoomRepository
EquipmentRepository
OrganizerRepository
ReservationRepository
```

### `model`

Contient les entités JPA correspondant aux principales données de l'application.

```text
Building
Room
Equipment
Organizer
Reservation
```

### `request`

Contient les DTO utilisés pour recevoir les données des requêtes HTTP.

Exemples :

```text
CreateBuildingRequest
CreateRoomRequest
CreateEquipmentRequest
CreateOrganizerRequest
CreateReservationRequest
AutomaticReservationRequest
```

### `response`

Contient les DTO utilisés pour retourner les données au client.

Exemples :

```text
ReservationResponse
RoomSummaryResponse
OrganizerSummaryResponse
ApiErrorResponse
```

### `exception`

Contient les exceptions métier ainsi que le gestionnaire global des erreurs.

---

# 4. Modèle métier

## Building

Un bâtiment possède :

```text
id
name
numberOfFloors
```

Le nom du bâtiment doit être unique sans tenir compte de la casse.

Le nombre d'étages doit être compris entre :

```text
1 et 200
```

Les étages commencent à `0`.

Par exemple, un bâtiment ayant :

```text
numberOfFloors = 5
```

possède les étages :

```text
0
1
2
3
4
```

---

## Room

Une salle possède notamment :

```text
id
name
building
floor
capacity
status
equipment
```

Le statut d'une salle peut être :

```text
AVAILABLE
MAINTENANCE
```

Une salle nouvellement créée est disponible par défaut.

Une salle en maintenance ne peut pas être utilisée pour une nouvelle réservation.

La capacité doit être strictement positive.

---

## Equipment

Un équipement possède :

```text
id
code
label
```

Le code doit être unique et respecter le format :

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

Exemples invalides :

```text
projector
_projector
A
```

---

## Organizer

Un organisateur possède :

```text
id
name
email
building
floor
```

L'adresse e-mail doit être valide et unique.

La localisation de l'organisateur est utilisée lors de l'attribution automatique pour calculer la distance avec les salles.

---

## Reservation

Une réservation contient notamment :

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

Le statut est :

```text
CONFIRMED
```

ou :

```text
CANCELLED
```

Une réservation annulée reste dans la base mais ne bloque plus la salle.

---

# 5. Règles de réservation

## Période

Une réservation doit respecter plusieurs règles.

Le début doit être strictement antérieur à la fin :

```text
start < end
```

Le début ne doit pas être dans le passé.

La durée maximale d'une réservation est :

```text
8 heures
```

Les dates sont envoyées au format ISO 8601 avec un fuseau ou un décalage UTC.

Exemple :

```text
2026-10-15T14:00:00+02:00
```

---

# 6. Détection des conflits

Deux réservations confirmées ne peuvent pas se chevaucher dans la même salle.

Il y a conflit lorsque :

```text
existingReservation.start < newReservation.end
AND
existingReservation.end > newReservation.start
```

Exemple de conflit :

```text
Réservation existante : 10:00 - 12:00
Nouvelle réservation  : 11:00 - 13:00
```

La nouvelle réservation doit être refusée.

En revanche, deux réservations consécutives sont autorisées :

```text
Réservation 1 : 10:00 - 11:00
Réservation 2 : 11:00 - 12:00
```

Une réservation `CANCELLED` n'est pas considérée comme bloquante.

---

# 7. Réservation manuelle

Le client peut choisir directement une salle grâce à :

```text
POST /api/reservations
```

La salle choisie est acceptée uniquement si :

- elle existe ;
- elle est `AVAILABLE` ;
- sa capacité est suffisante ;
- elle possède tous les équipements demandés ;
- aucune réservation confirmée ne chevauche la période.

Si une de ces conditions n'est pas respectée, la demande est refusée.

Le système ne remplace jamais automatiquement une salle choisie manuellement par une autre salle.

---

# 8. Attribution automatique d'une salle

L'application peut également choisir automatiquement la salle la plus adaptée.

Endpoint :

```text
POST /api/reservations/automatic
```

Le client ne fournit pas de `roomId`.

Le système commence par éliminer toutes les salles incompatibles.

Une salle candidate doit :

```text
status = AVAILABLE
capacity >= numberOfParticipants
contenir tous les équipements demandés
ne pas avoir de réservation confirmée en conflit
```

Une fois les salles compatibles trouvées, l'application calcule un score pour chacune d'elles.

---

## Calcul de la capacité inutilisée

```text
unusedCapacity =
room.capacity - numberOfParticipants
```

Exemple :

```text
capacity = 30
participants = 25

unusedCapacity = 5
```

---

## Calcul de la distance

### Même bâtiment

Si l'organisateur et la salle sont dans le même bâtiment :

```text
distance =
abs(room.floor - organizer.floor)
```

Exemple :

```text
organizer.floor = 2
room.floor = 5

distance = 3
```

### Bâtiments différents

Si les bâtiments sont différents :

```text
distance =
10 + abs(room.floor - organizer.floor)
```

Exemple :

```text
organizer.floor = 2
room.floor = 5

distance = 10 + 3
distance = 13
```

Le changement de bâtiment ajoute donc une pénalité importante.

---

## Calcul du score

Le score final est :

```text
score =
distance * 10 + unusedCapacity
```

Exemple :

```text
distance = 3
unusedCapacity = 5

score = 3 * 10 + 5
score = 35
```

La salle ayant le score le plus faible est sélectionnée.

---

## Gestion des égalités

Lorsque plusieurs salles ont exactement le même score, elles sont départagées :

```text
1. par nom de salle dans l'ordre alphabétique
2. puis par identifiant
```

La comparaison des noms ne tient pas compte de la casse.

Cette règle garantit un résultat déterministe.

---

# 9. Recherche de salles disponibles

Endpoint :

```text
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

La recherche élimine les salles :

```text
en maintenance
trop petites
sans les équipements demandés
déjà réservées sur la période
```

Les salles compatibles sont triées par :

```text
1. unusedCapacity croissant
2. nom sans tenir compte de la casse
3. identifiant
```

Si aucune salle n'est compatible :

```json
[]
```

est retourné.

---

# 10. Endpoints de l'API

## Buildings

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/buildings` | Créer un bâtiment |
| GET | `/api/buildings` | Lister les bâtiments |
| GET | `/api/buildings/{buildingId}` | Consulter un bâtiment |
| PUT | `/api/buildings/{buildingId}` | Modifier un bâtiment |

---

## Rooms

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/rooms` | Créer une salle |
| GET | `/api/rooms` | Lister les salles |
| GET | `/api/rooms/{roomId}` | Consulter une salle |
| PUT | `/api/rooms/{roomId}` | Modifier une salle |
| GET | `/api/rooms/available` | Rechercher des salles disponibles |
| PATCH | `/api/rooms/{roomId}/status` | Modifier le statut |
| PUT | `/api/rooms/{roomId}/equipment` | Remplacer les équipements |

---

## Equipment

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/equipment` | Créer un équipement |
| GET | `/api/equipment` | Lister les équipements |

---

## Organizers

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/organizers` | Créer un organisateur |
| GET | `/api/organizers` | Lister les organisateurs |
| GET | `/api/organizers/{organizerId}` | Consulter un organisateur |

---

## Reservations

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/reservations` | Réserver une salle précise |
| POST | `/api/reservations/automatic` | Attribution automatique |
| GET | `/api/reservations` | Lister et filtrer les réservations |
| GET | `/api/reservations/{reservationId}` | Consulter une réservation |
| PATCH | `/api/reservations/{reservationId}/cancel` | Annuler une réservation |

---

# 11. Filtres des réservations

La route :

```text
GET /api/reservations
```

accepte plusieurs paramètres optionnels.

```text
roomId
organizerId
from
to
```

Exemple :

```text
GET /api/reservations?roomId=1
```

ou :

```text
GET /api/reservations
    ?organizerId=2
    &from=2026-10-15T10:00:00+02:00
    &to=2026-10-15T18:00:00+02:00
```

Lorsque `from` et `to` sont fournis :

```text
from < to
```

doit être respecté.

Sinon l'API retourne :

```text
INVALID_RESERVATION_PERIOD
```

---

# 12. Exemple complet d'utilisation

Une utilisation typique de l'API peut suivre l'ordre suivant.

## Étape 1 — Créer un bâtiment

```http
POST /api/buildings
Content-Type: application/json
```

```json
{
  "name": "Building A",
  "numberOfFloors": 5
}
```

Réponse :

```text
201 Created
```

---

## Étape 2 — Créer un équipement

```http
POST /api/equipment
Content-Type: application/json
```

```json
{
  "code": "PROJECTOR",
  "label": "Video projector"
}
```

---

## Étape 3 — Créer une salle

```http
POST /api/rooms
Content-Type: application/json
```

```json
{
  "name": "Orion",
  "buildingId": 1,
  "floor": 2,
  "capacity": 30,
  "equipmentCodes": [
    "PROJECTOR"
  ]
}
```

Une nouvelle salle reçoit automatiquement le statut :

```text
AVAILABLE
```

---

## Étape 4 — Créer un organisateur

```http
POST /api/organizers
Content-Type: application/json
```

```json
{
  "name": "Alice Martin",
  "email": "alice@example.org",
  "buildingId": 1,
  "floor": 2
}
```

---

## Étape 5 — Créer une réservation manuelle

```http
POST /api/reservations
Content-Type: application/json
```

```json
{
  "title": "Project meeting",
  "organizerId": 1,
  "roomId": 1,
  "start": "2026-10-15T14:00:00+02:00",
  "end": "2026-10-15T16:00:00+02:00",
  "numberOfParticipants": 20,
  "requiredEquipmentCodes": [
    "PROJECTOR"
  ]
}
```

---

## Étape 6 — Demander une attribution automatique

```http
POST /api/reservations/automatic
Content-Type: application/json
```

```json
{
  "title": "Automatic meeting",
  "organizerId": 1,
  "start": "2026-10-16T10:00:00+02:00",
  "end": "2026-10-16T11:00:00+02:00",
  "numberOfParticipants": 20,
  "requiredEquipmentCodes": [
    "PROJECTOR"
  ]
}
```

Le système sélectionne automatiquement la meilleure salle compatible.

---

# 13. Format des réponses de réservation

Une réservation est retournée sous une forme similaire à :

```json
{
  "id": 1,
  "title": "Project meeting",
  "status": "CONFIRMED",
  "room": {
    "id": 1,
    "name": "Orion",
    "building": {
      "id": 1,
      "name": "Building A",
      "numberOfFloors": 5
    },
    "floor": 2,
    "capacity": 30,
    "status": "AVAILABLE"
  },
  "organizer": {
    "id": 1,
    "name": "Alice Martin",
    "building": {
      "id": 1,
      "name": "Building A",
      "numberOfFloors": 5
    },
    "floor": 2
  },
  "start": "2026-10-15T14:00:00+02:00",
  "end": "2026-10-15T16:00:00+02:00",
  "numberOfParticipants": 20,
  "requiredEquipmentCodes": [
    "PROJECTOR"
  ],
  "createdAt": "2026-10-01T13:45:12Z"
}
```

---

# 14. Gestion des erreurs

Toutes les erreurs utilisent une structure commune.

Exemple :

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

Les champs principaux sont :

```text
code
message
timestamp
path
details
fieldErrors
```

`details` contient les informations métier liées à l'erreur.

`fieldErrors` contient les erreurs liées à la validation des champs.

---

# 15. Principaux codes d'erreur

| Code | HTTP | Description |
|---|---:|---|
| `VALIDATION_ERROR` | 400 | Une donnée reçue est invalide |
| `INVALID_RESERVATION_PERIOD` | 400 | Période incorrecte |
| `BUILDING_NOT_FOUND` | 404 | Bâtiment inexistant |
| `ROOM_NOT_FOUND` | 404 | Salle inexistante |
| `ORGANIZER_NOT_FOUND` | 404 | Organisateur inexistant |
| `RESERVATION_NOT_FOUND` | 404 | Réservation inexistante |
| `EQUIPMENT_NOT_FOUND` | 404 | Équipement inexistant |
| `ROOM_CAPACITY_EXCEEDED` | 409 | Capacité insuffisante |
| `MISSING_REQUIRED_EQUIPMENT` | 409 | Équipement manquant |
| `ROOM_ALREADY_RESERVED` | 409 | Conflit avec une réservation |
| `ROOM_UNAVAILABLE` | 409 | Salle en maintenance |
| `NO_COMPATIBLE_ROOM` | 409 | Aucune salle compatible |
| `RESERVATION_ALREADY_CANCELLED` | 409 | Réservation déjà annulée |
| `RESOURCE_ALREADY_EXISTS` | 409 | Ressource unique déjà existante |
| `BUILDING_FLOOR_COUNT_CONFLICT` | 409 | Nombre d'étages incompatible |

---

# 16. Base de données

Le projet utilise une base :

```text
H2
```

en mémoire.

Cela signifie que les données sont supprimées lorsque l'application est arrêtée puis redémarrée.

Le schéma de base de données est géré avec :

```text
Flyway
```

Les migrations sont situées dans :

```text
src/main/resources/db/migration
```

Le fichier principal de migration crée notamment les tables nécessaires pour :

```text
buildings
rooms
equipment
room_equipment
organizers
reservations
reservation_required_equipment
```

---

# 17. Installation

## Prérequis

Il faut disposer de :

```text
Java 21
Git
```

Maven n'a pas besoin d'être installé globalement.

Le projet contient le Maven Wrapper :

```text
mvnw
mvnw.cmd
```

---

# 18. Cloner le projet

```bash
git clone <URL_DU_REPOSITORY>
```

Puis :

```bash
cd meetingRoomReservation-RHA
```

---

# 19. Lancer l'application

Sous Windows :

```powershell
.\mvnw.cmd spring-boot:run
```

Sous Linux ou macOS :

```bash
./mvnw spring-boot:run
```

Le contrat OpenAPI utilise par défaut :

```text
http://localhost:8080
```

Si un autre port est configuré localement dans `application.properties`, il faut utiliser ce port à la place.

Par exemple :

```text
http://localhost:8081
```

---

# 20. Lancer les tests

Sous Windows :

```powershell
.\mvnw.cmd test
```

Sous Linux ou macOS :

```bash
./mvnw test
```

Un build réussi se termine par :

```text
BUILD SUCCESS
```

---

# 21. Tests unitaires

Les tests unitaires vérifient la logique métier indépendamment de la base de données.

Ils couvrent notamment :

- capacité exactement suffisante ;
- salle trop petite ;
- salle en maintenance ;
- salle déjà réservée ;
- équipement demandé absent ;
- réservations consécutives ;
- distance dans le même bâtiment ;
- distance entre bâtiments différents ;
- calcul du score ;
- sélection de la salle au meilleur score ;
- départage alphabétique ;
- départage par identifiant ;
- absence de salle compatible.

Les tests utilisent notamment :

```text
JUnit 5
Mockito
```

---

# 22. Tests d'intégration

Les tests d'intégration chargent l'application Spring Boot et utilisent une vraie base H2 de test.

Ils couvrent notamment :

- création d'une salle ;
- consultation d'une salle ;
- création d'une réservation ;
- attribution automatique ;
- recherche des salles disponibles ;
- ordre des salles disponibles ;
- refus d'une réservation conflictuelle ;
- annulation d'une réservation ;
- nouvelle réservation après annulation ;
- format des réponses d'erreur.

Les tests suivent autant que possible la structure :

```text
GIVEN
WHEN
THEN
```

---

# 23. Intégration continue

Le projet utilise :

```text
GitHub Actions
```

Le workflow est défini dans :

```text
.github/workflows/ci.yml
```

Il se déclenche automatiquement sur la branche :

```text
main
```

notamment lors d'un `push` ou d'une `pull_request`.

La CI contient deux jobs.

## Lint

Le job :

```text
Lint
```

exécute Checkstyle afin de vérifier automatiquement certaines règles de qualité du code Java, notamment les imports inutilisés et les tabulations.

Commande utilisée :

```bash
./mvnw checkstyle:check
```

## Tests

Le job :

```text
Tests
```

exécute tous les tests Maven.

Commande utilisée :

```bash
./mvnw test
```

La CI doit être entièrement verte avant la livraison du projet.

---

# 24. Validation des données

Jakarta Validation est utilisée sur les DTO de requête.

Exemples de contraintes :

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

Exemple pour un code équipement :

```text
^[A-Z][A-Z0-9_]{1,49}$
```

Une erreur de validation retourne :

```text
HTTP 400
VALIDATION_ERROR
```

Les champs invalides sont détaillés dans :

```text
fieldErrors
```

---

# 25. Statuts HTTP principaux

L'API utilise notamment :

```text
200 OK
201 Created
400 Bad Request
404 Not Found
409 Conflict
```

Les créations utilisent :

```text
201 Created
```

et ajoutent un header :

```text
Location
```

permettant d'identifier la ressource créée.

---

# 26. Documentation OpenAPI

La référence principale de l'API est :

```text
openapi.yml
```

Ce fichier décrit précisément :

- les endpoints ;
- les paramètres ;
- les request bodies ;
- les réponses ;
- les codes HTTP ;
- les contraintes ;
- les objets JSON ;
- les erreurs possibles.

En cas de doute concernant le contrat HTTP, `openapi.yml` constitue la référence du projet.

---

# 27. Principales règles métier

En résumé :

```text
Une salle en maintenance ne peut pas être réservée.

Une salle doit avoir une capacité suffisante.

Une salle doit posséder tous les équipements demandés.

Deux réservations confirmées ne peuvent pas se chevaucher.

Deux réservations consécutives sont autorisées.

Une réservation annulée ne bloque plus la salle.

Une réservation ne peut pas dépasser huit heures.

Une attribution automatique choisit la salle compatible ayant le score minimal.

En cas d'égalité de score :
nom alphabétique → identifiant.
```

---

# 28. Exemple de scénario métier

Supposons un organisateur situé :

```text
Building A
floor = 2
```

Deux salles sont disponibles.

### Room Alpha

```text
building = Building A
floor = 2
capacity = 25
participants = 20
```

Calcul :

```text
distance = 0
unusedCapacity = 5

score = 0 * 10 + 5
score = 5
```

### Room Beta

```text
building = Building A
floor = 4
capacity = 20
participants = 20
```

Calcul :

```text
distance = 2
unusedCapacity = 0

score = 2 * 10 + 0
score = 20
```

La salle sélectionnée est donc :

```text
Room Alpha
```

car :

```text
5 < 20
```

---

# 29. Persistance et redémarrage

La base H2 utilisée actuellement est une base en mémoire.

Après un redémarrage de l'application, les données créées précédemment ne sont donc plus présentes.

Pour tester manuellement l'API après un redémarrage, il faut recréer les ressources nécessaires dans cet ordre :

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

# 30. Git et versionnement

Le projet utilise Git pour conserver l'historique des évolutions.

Les commits doivent avoir des noms explicites.

Exemples :

```text
Add reservation validation

Add reservation service unit tests

Add required integration tests

Complete API error details and validation

Add GitHub Actions CI
```

Avant une livraison, il est recommandé de vérifier :

```bash
git status
```

puis de lancer :

```powershell
.\mvnw.cmd test
```

avant le dernier push.

---

# 31. Auteur

Projet réalisé dans le cadre du cours de **Java avancé – ISMIN**.

---

# 32. Résumé

Ce projet met en œuvre une API REST complète de réservation de salles avec :

```text
Spring Boot
JPA / Hibernate
Flyway
H2
Validation
Gestion d'erreurs
Tests unitaires
Tests d'intégration
Attribution automatique
Git
GitHub Actions
```

La fonctionnalité principale est l'attribution automatique d'une salle en fonction :

```text
de sa disponibilité,
de sa capacité,
de ses équipements,
de sa localisation,
et des conflits de réservation.
```

Le projet est testé automatiquement à chaque modification de la branche `main` grâce à GitHub Actions.
````