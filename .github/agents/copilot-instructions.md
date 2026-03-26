# admin Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-22

## Active Technologies
- Java 21 + Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6, JWT, Lombok, JUnit 5 (003-game-management)
- MySQL 8.3 (AWS RDS) — entities: `lottery`, `lottery_prize`, `lottery_draw_record`, `lottery_lock` (003-game-management)
- Draw mechanics: weighted random 1/N, opener protection lock (DB-level + FOR UPDATE), last-prize guarantee, auto price reduction (003-game-management)
- Java 21 + Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6, JWT (jjwt 0.9.1), Lombok, Jakarta Validation (002-express-shipping)
- MySQL 8.3 — all entities mapped via MyBatis; UUID PKs (String), timestamps as `LocalDateTime` (002-express-shipping)
- Java 21 + Spring Boot 3.3.3, MyBatis 3.0.5, Spring Security 6 + JWT, Lombok, SpringDoc OpenAPI (Swagger) (008-order-management)
- MySQL 8.3 (AWS RDS) — tables `order`, `order_item`, `order_status_log`, `prize_box` (008-order-management)
- Java 21 + Spring Boot 3.3.3, Spring Security 6, MyBatis 3.0.5, JJWT 0.9.1, MySQL Connector 8.x (009-rbac-permissions)
- MySQL 8.3 (AWS RDS) — tables `role`, `menu`, `role_menu`, `admin_user_role`, `store_user` already exist (009-rbac-permissions)
- Java 21 + Spring Boot 3.3.3 · MyBatis 3.0.5 · Spring Security · JWT · Lombok (006-payment-points)
- Java 21 + Spring Boot 3.3.3, MyBatis 3.0.5 (mybatis-spring-boot-starter), Spring Security + JWT (jjwt 0.9.1), Lombok 1.18.32, Springdoc OpenAPI 2.3.0 (005-lottery-ticket-system)
- Java 21 + Spring Boot 3.3.3, Spring Security 6, MyBatis 3.0.5, MyBatis Generator (Example pattern), JWT (jjwt), Lombok (012-referral-code)
- MySQL 8.3 — tables `referral_code`, `referral_record`; FK relationships to `store` and `user` (012-referral-code)
- [if applicable, e.g., PostgreSQL, CoreData, files or N/A] (main)

- **Java 21** + **Spring Boot 3.3.3** + **MyBatis 3.0.5** + **Spring Security 6** + **JWT** (007-news-management)
- **MySQL 8.3** on AWS RDS — primary data store
- **AWS EC2 Linux** — deployment target
- **Lombok** — boilerplate reduction
- **SpringDoc OpenAPI** (Swagger UI) — API docs at `/swagger-ui/index.html`
- **JUnit 5** + **Spring Boot Test** — testing framework

## Project Structure

```text
src/main/java/com/group/admin/
├── config/          # SecurityConfig, CorsConfig, MyBatisConfig, etc.
├── controller/
│   ├── admin/       # Admin-only: @PreAuthorize("hasRole('ADMIN')")
│   └── api/         # Public frontend endpoints (no auth, /api/ prefix)
├── entity/          # MyBatis entities (MBG-generated)
├── example/         # MyBatis Example criteria builders (MBG-generated)
├── mapper/          # MyBatis interfaces + XML in resources/mapper/
├── req/             # Request DTOs (subdirectory per feature)
├── res/             # Response DTOs (subdirectory per feature, @Builder)
├── scheduler/       # Spring @Scheduled tasks
├── service/         # Service interfaces
└── service/impl/    # Service implementations
```

## Commands

```bash
mvn clean package -DskipTests   # Build
mvn spring-boot:run             # Run dev
mvn test                        # Test
```

## Code Style

- Admin controllers: class-level `@PreAuthorize("hasRole('ADMIN')")`
- Public controllers: `@RequestMapping("/api/...")` prefix
- Service impls: `@Transactional` on writes; UUID PKs via `UUID.randomUUID().toString()`
- Response DTOs: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
- Logging: emoji prefixes (✅❌⚠️🔍➕✏️🗑️)

## Recent Changes
- main: Added [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION] + [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]
- main: Added [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION] + [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION]
- 012-referral-code: Added Java 21 + Spring Boot 3.3.3, Spring Security 6, MyBatis 3.0.5, MyBatis Generator (Example pattern), JWT (jjwt), Lombok

  - Entity: News (UUID PK, DRAFT/PUBLISHED/UNPUBLISHED, scheduled publish/unpublish)
  - Admin: POST/PUT/DELETE/GET /admin/news, GET /admin/news/{id}
  - Public: GET /api/news, GET /api/news/{id}
  - Scheduler: @Scheduled(fixedRate=60000) auto-publish + auto-unpublish

<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
