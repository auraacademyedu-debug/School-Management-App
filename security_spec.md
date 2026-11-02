# EduPulse Security Specification

## 1. Data Invariants
1. Every document across `/users/{userId}/...` subcollections (`config`, `enquiries`, `students`, `fee_transactions`, `staff`, `records`) MUST belong strictly to the authenticated user (`request.auth != null && request.auth.uid == userId && incoming().userId == userId`).
2. Document IDs (`configId`, `enquiryId`, `studentId`, `txId`, `staffId`, `recordId`) MUST match `^[a-zA-Z0-9_\-]+$` and be `<= 128` characters on single-document operations (`get`, `create`, `update`, `delete`).
3. Every string field MUST be size-bounded (`<= 500` or specific field cap) to prevent resource exhaustion ("Denial of Wallet").
4. Timestamps (`createdAt`, `updatedAt`) MUST be server timestamps (`is timestamp && <= request.time`), and `createdAt` + `userId` MUST be immutable on updates.
5. `allow update` blocks MUST wrap all update logic inside `isValid[Entity](incoming())` and explicitly constrain mutated keys with `incoming().diff(existing()).affectedKeys().hasOnly(...)`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read**: `GET /users/alice/enquiries` with `auth == null` -> REJECT.
2. **Cross-Tenant PII Scrape**: `GET /users/bob/enquiries/enq_1` as `alice` -> REJECT.
3. **Owner UID Spoofing**: `CREATE /users/alice/enquiries/enq_1` with `{ userId: "bob", ... }` as `alice` -> REJECT.
4. **Ghost Field Shadow Update**: `UPDATE /users/alice/enquiries/enq_1` adding `{ isSuperAdmin: true }` -> REJECT via `hasOnly()`.
5. **Immutable Timestamp Mutation**: `UPDATE /users/alice/students/stu_1` modifying `createdAt` -> REJECT.
6. **Future Timestamp Spoofing**: `CREATE /users/alice/enquiries/enq_1` with `createdAt = request.time + 1h` -> REJECT.
7. **ID Poisoning**: `CREATE /users/alice/enquiries/bad$id!@#` -> REJECT via `isValidId()`.
8. **Denial-of-Wallet Oversized String**: `CREATE /users/alice/enquiries/enq_1` with 10KB `remarks` -> REJECT via `.size() <= 500`.
9. **Enum Value Poisoning**: `CREATE /users/alice/enquiries/enq_1` with `status: "Hacked"` -> REJECT via enum allowlist.
10. **Negative Fee Injection**: `CREATE /users/alice/fee_transactions/tx_1` with `amount: -5000` -> REJECT via `amount >= 0`.
11. **Type Confusion on Update**: `UPDATE /users/alice/config/main` setting `defaultFormPrice: "free"` -> REJECT via `isValidSchoolConfig(incoming())`.
12. **Blanket Collection List**: `LIST /users/bob/students` by `alice` -> REJECT via `request.auth.uid == userId && resource.data.userId == request.auth.uid`.
