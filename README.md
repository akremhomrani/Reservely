# RéservTN — Backend (Microservices)

> Plateforme de réservation multi-services pour la Tunisie
> *"Réservez en 3 clics" / "احجز في 3 نقرات"*

## Stack Technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Java 17 |
| Framework | Spring Boot 3.x + Maven |
| IAM / Auth | Keycloak (OIDC / OAuth2) |
| Base de données | PostgreSQL 15 (une DB par service) |
| Cache | Redis 7 |
| Messaging | RabbitMQ |
| Service Discovery | Eureka Server |
| API Gateway | Spring Cloud Gateway |
| Config | Spring Cloud Config |
| Migrations | Flyway |
| Tests | JUnit 5 + Testcontainers |
| Conteneurs | Docker + Docker Compose |

---

## Architecture Microservices

```
reservtn-backend/
├── api-gateway/           ← Port 8080 — Point d'entrée unique (JWT Keycloak, rate limiting)
├── discovery-service/     ← Port 8761 — Eureka Server
├── config-service/        ← Port 8888 — Spring Cloud Config (centralisation yml)
├── business-service/      ← Port 8081 — Établissements, services, staff, horaires
├── booking-service/       ← Port 8082 — Réservations, disponibilités, anti-conflit
├── notification-service/  ← Port 8083 — SMS (WinSMS) + Email (Mailgun)
├── review-service/        ← Port 8084 — Avis clients post-prestation
├── search-service/        ← Port 8085 — Recherche géo + texte (PG trigram + GIST)
└── admin-service/         ← Port 8086 — Validation établissements, stats plateforme
```

---

## Sécurité — Keycloak

```
Realm: reservtn
├── Clients
│   ├── reservtn-gateway  (confidentiel — backend)
│   └── reservtn-web      (public — Angular SPA, PKCE)
└── Roles
    ├── CUSTOMER
    ├── BUSINESS_OWNER
    ├── STAFF
    └── ADMIN
```

**Flow :**
1. Angular → Keycloak (login)
2. Keycloak → Access Token JWT + Refresh Token
3. Angular → API Gateway (Authorization: Bearer ...)
4. Gateway valide JWT → route vers le bon microservice
5. Chaque service valide le token localement (Keycloak public key)

**OTP SMS :** Keycloak Custom SPI → WinSMS (auth primaire tunisienne)

---

## Communication Inter-Services

```
RabbitMQ Topics
├── business.created      (business-service → ...)
├── business.approved     (admin-service → booking-service)
├── booking.confirmed     (booking-service → notification-service)
├── booking.cancelled     (booking-service → notification-service)
└── booking.reminder      (booking-service → notification-service)
```

Pas d'appels synchrones entre services métier (sauf search-service → business-service via Feign).

---

## Modèle de Données (par service)

### business-service (`db_business`)
```
businesses             → uuid, owner_keycloak_id, name_fr, name_ar, category,
                         status (PENDING_APPROVAL | ACTIVE | SUSPENDED),
                         city, governorate, lat, lng, rating_average
business_working_hours → business_id, day_of_week, open_time, close_time
service_items          → uuid, business_id, name_fr, name_ar, duration_minutes,
                         price (DECIMAL 10,3 — TND)
staff                  → uuid, business_id, keycloak_user_id (nullable), is_bookable
staff_services         → staff_id ↔ service_item_id (M2M)
```

**Catégories :**
`BARBER | BEAUTY_SALON | RESTAURANT | CAR_RENTAL | SPA | DENTAL | FITNESS | OTHER`

### booking-service (`db_booking`)
```
bookings              → uuid, reference_code (RES-2024-XXXXX),
                        customer_keycloak_id, business_id, service_item_id,
                        staff_id (nullable), start_datetime, end_datetime,
                        status (PENDING|CONFIRMED|COMPLETED|CANCELLED|NO_SHOW),
                        total_amount (TND), notes, guest_phone
availability_blocks   → blocages ponctuels ou récurrents (RFC 5545 RRULE)
```

---

## Prérequis

```bash
java -version    # Java 17 requis
mvn -version     # Maven 3.9+
docker --version # Docker 24+
docker compose version
```

---

## Démarrage Local

### 1. Variables d'environnement
```bash
cp .env.example .env
# Remplir : KEYCLOAK_*, DB_*, RABBITMQ_*, WINSMS_*, MAILGUN_*
```

### 2. Lancer l'infrastructure
```bash
docker compose up -d keycloak postgres redis rabbitmq
```

### 3. Lancer les services (ordre obligatoire)
```bash
# 1. Discovery
cd discovery-service && mvn spring-boot:run

# 2. Config
cd config-service && mvn spring-boot:run

# 3. Services métier (dans n'importe quel ordre)
cd business-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd review-service && mvn spring-boot:run
cd search-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run

# 4. Gateway (en dernier)
cd api-gateway && mvn spring-boot:run
```

### 4. Vérification
- Eureka Dashboard : http://localhost:8761
- Keycloak Admin : http://localhost:8180 (admin/admin)
- API Gateway : http://localhost:8080
- RabbitMQ Management : http://localhost:15672

---

## Tests

```bash
# Tous les tests (Testcontainers requis — Docker doit tourner)
mvn test

# Service spécifique
cd booking-service && mvn test

# Tests d'intégration uniquement
mvn test -Dgroups=integration
```

**Couverture cible : 80%+**

---

## Conventions

- Commits : `feat:`, `fix:`, `test:`, `refactor:`, `docs:`
- Pas de commit direct sur `main`
- Chaque PR passe les tests CI GitHub Actions
- Pas de secrets dans le code — tout dans `.env` + Spring Cloud Config
- Logs : INFO/WARN/ERROR uniquement, jamais de données sensibles (tokens, OTP, passwords)

---

## Phases de Développement

| Phase | Contenu | Durée |
|-------|---------|-------|
| 1 | Infrastructure (Keycloak, Gateway, Eureka, Config) | 2 semaines |
| 2 | business-service (CRUD + Flyway) | 2 semaines |
| 3 | booking-service (disponibilités + anti-conflit) | 2 semaines |
| 4 | notification-service (WinSMS + Mailgun) | 1 semaine |
| 5 | search-service + review-service | 1 semaine |
| 6 | admin-service + analytics | 1 semaine |
