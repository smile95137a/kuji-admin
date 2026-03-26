# Implementation Plan: 商品與抽獎管理 (Product & Lottery Management)

**Branch**: `011-product-lottery` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/011-product-lottery/spec.md`

## Summary

Implement the full Product/Lottery Management system for the KUJI platform. This covers the complete lifecycle of a lottery product — from creation (DRAFT) through configuration, scheduling, and live drawing (IN_PROGRESS) to automatic SOLD_OUT — along with prize pool management (including `recycleBonus`), protection-round locking, last-prize (最後賞) and auto-discount mechanisms, and a product-copy feature for rapid creation of similar products.

The approach extends the existing `Lottery`, `LotteryPrize`, `LotteryDrawRecord`, and `LotterySession` entities already in the codebase, adds the two missing lifecycle states (`CONFIGURED`, `DRAWABLE`), adds the `recycleBonus` column to `lottery_prize`, wires `@Scheduled` polling for scheduled status transitions, and exposes the required admin and public REST API endpoints.

## Technical Context

**Language/Version**: Java 21  
**Framework**: Spring Boot 3.3.3  
**Primary Dependencies**: MyBatis 3.0.5, Spring Security + JWT, Lombok  
**Storage**: MySQL 8.3 — existing `lottery`, `lottery_prize`, `lottery_draw_record`, `lottery_session`, `lottery_lock` tables (plus new `protection_round` alias = `lottery_session`)  
**Testing**: JUnit 5 + Spring Boot Test (`@SpringBootTest`, `@MockBean`, `@DataJdbcTest`)  
**Target Platform**: AWS EC2 Linux  
**Project Type**: REST API web-service (single Spring Boot application, package `com.group.admin`)  
**Performance Goals**: Draw latency < 1 s (SC-002); sold-out transition within 5 s of last draw (SC-005)  
**Constraints**: `< 200 ms p95` for read endpoints; optimistic/pessimistic locking on concurrent last-ticket draw; no JPA — plain MyBatis POJO pattern  
**Scale/Scope**: Multi-store; each store owns its lottery products; Admin can manage all stores

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

> **Note**: The project constitution (`constitution.md`) is currently unpopulated (template placeholders only). No blocking principles are defined. The following domain-level gates are applied based on the spec and codebase conventions:

| Gate | Status | Notes |
|------|--------|-------|
| Entity follows plain-POJO MyBatis pattern | ✅ PASS | Existing `Lottery`, `LotteryPrize` are plain POJOs; new fields follow same pattern |
| UUID string PKs | ✅ PASS | All new entities use `UUID.randomUUID().toString()` |
| Response wrapped in `ApiResponse<T>` | ✅ PASS | GlobalResponseAspect handles wrapping automatically |
| No JPA annotations | ✅ PASS | Confirmed — only MyBatis XML mappers |
| Status transitions are server-side only | ✅ PASS | Clients send transition requests; server validates FSM |
| Scheduled tasks use `@Scheduled` in `ScheduledTasks` | ✅ PASS | Existing scheduler component extended, not duplicated |
| Draw concurrency handled with DB-level locking | ✅ PASS | `lottery_lock` + `LotteryLockService` already present |
| Admin endpoints under `/admin/**`, public under `/api/**` | ✅ PASS | Existing controller split enforced |

**Post-Design Re-check**: ✅ All gates pass after Phase 1 design. No complexity violations.

## Project Structure

### Documentation (this feature)

```text
specs/011-product-lottery/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   ├── POST_admin_lottery.md
│   ├── PUT_admin_lottery_{id}.md
│   ├── POST_admin_lottery_{id}_copy.md
│   ├── PUT_admin_lottery_{id}_status.md
│   ├── GET_api_lottery.md
│   └── GET_api_lottery_{id}.md
└── tasks.md             ← Phase 2 output (/speckit.tasks — NOT created here)
```

### Source Code (repository root)

```text
src/main/java/com/group/admin/
├── entity/
│   ├── Lottery.java                  ← ADD: configuredAt, drawableAt, remainingDraws,
│   │                                      lastPrizeMode, lastPrizePoolEnabled,
│   │                                      discountTriggerLevel, sourceLotteryId
│   ├── LotteryPrize.java             ← ADD: recycleBonus (Long)
│   ├── LotteryDrawRecord.java        ← existing (no change)
│   └── LotterySession.java           ← existing (ProtectionRound alias)
├── enums/
│   └── LotteryStatusEnum.java        ← ADD: CONFIGURED, DRAWABLE, SOLD_OUT
├── controller/
│   ├── admin/
│   │   └── AdminLotteryController.java  ← ADD: createLottery, updateLottery,
│   │                                          copyLottery, changeStatus endpoints
│   └── api/
│       └── LotteryBrowseController.java ← ADD: public list + detail endpoints
├── service/
│   ├── LotteryService.java / LotteryServiceImpl.java
│   │                                 ← ADD: create, update, copy, changeStatus,
│   │                                        auto-transition logic
│   └── LotteryPrizeService.java / LotteryPrizeServiceImpl.java
│                                     ← ADD: recycleBonus handling
├── req/
│   ├── CreateLotteryReq.java
│   ├── UpdateLotteryReq.java
│   ├── LotteryStatusChangeReq.java
│   └── LotteryListReq.java
├── res/
│   ├── LotteryDetailRes.java
│   └── LotteryListItemRes.java
├── mapper/
│   ├── LotteryMapper.java            ← ADD: selectScheduledForPromotion, 
│   │                                        selectDrawableForStart
│   └── LotteryPrizeMapper.java       ← existing (recycleBonus column auto-mapped)
└── scheduler/
    └── ScheduledTasks.java           ← ADD: lotteryStatusTransitionTask (every 60 s)

src/main/resources/
└── mapper/
    ├── LotteryMapper.xml             ← ADD: new columns + queries
    └── LotteryPrizeMapper.xml        ← ADD: recycle_bonus column mapping

src/main/resources/db/migration/      (or manual SQL scripts)
    └── V011__product_lottery_enhancements.sql
        ← ALTER TABLE lottery ADD COLUMN ...
        ← ALTER TABLE lottery_prize ADD COLUMN recycle_bonus BIGINT DEFAULT 0
        ← UPDATE lottery_status enum values

src/test/java/com/group/admin/
├── controller/
│   ├── AdminLotteryControllerTest.java
│   └── LotteryBrowseControllerTest.java
└── service/
    ├── LotteryServiceTest.java
    └── LotteryStatusTransitionTest.java
```

**Structure Decision**: Single Spring Boot project. All new code follows the existing `com.group.admin` package conventions. No new modules or sub-projects introduced.

## Complexity Tracking

> No constitution violations detected. No complexity justification required.
