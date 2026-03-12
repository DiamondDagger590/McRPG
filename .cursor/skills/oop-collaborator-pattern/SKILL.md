---
name: oop-collaborator-pattern
description: Prefer object collaborators over static utility helpers for domain behavior in McRPG.
---

## Use this skill when

- You are about to add a new helper class for gameplay/domain logic.
- You are extracting duplicate logic across slots, listeners, or managers.
- You notice a proposed `final class` with only `static` methods in non-DAO code.

## Core rule

If behavior needs runtime context (player, manager, offering, scope, config), model it as an object collaborator with instance methods and explicit dependencies.

## Preferred patterns

1. Create a small collaborator class with constructor-injected dependencies.
2. Keep the collaborator package-local unless a wider API is required.
3. Call the collaborator from owning objects (`Slot`, `Listener`, `Manager`) instead of static helpers.
4. Add/adjust focused unit tests around collaborator behavior.

## Avoid

- Static utility classes for domain logic.
- "God utility" files mixing unrelated concerns.
- Hiding dependency lookup inside static methods.

## Allowed exceptions

- DAO operations that are intentionally static by project convention.
- Pure constants or tiny stateless value-format helpers where no domain dependency exists.

## Quick checklist

- Does this logic require domain context? -> use object collaborator.
- Are dependencies explicit in constructor or method signature? -> yes.
- Can this be tested directly with mocks/fakes? -> yes.
- Is a static helper still necessary after design review? -> usually no.
