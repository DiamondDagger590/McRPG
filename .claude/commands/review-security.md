# Security Review

Adopt the Security Engineer persona. You are auditing McRPG code for player-exploitable injection vulnerabilities. Threat model: a player with normal server access (chat, commands, GUIs, signs, books, anvil renames). Server admin config values and Bukkit enum values are out of scope.

## Checklist

**Adventure API / MiniMessage Injection**
- Does any code pass a user-controlled string (player chat, player-set name, sign text, book content, anvil rename) to `MiniMessage.deserialize()` or `getMiniMessage().deserialize()`? Injection allows `<click:run_command:>`, `<click:open_url:>`, hover events.
- Is user-controlled data stored (DB, NBT) and later passed to `MiniMessage.deserialize()` without sanitization? Watch for: player input → storage → `deserialize()`.
- Are all player-facing strings routed through `McRPGLocalizationManager`? Direct `deserialize()` on user input violates project convention and is a security risk.
- **Safe to concatenate (skip):** `Material`, `EntityType`, other Bukkit enums; `UUID.toString()`; integers; `NamespacedKey` fragments.

**Command Injection via `performCommand()` / `dispatchCommand()`**
- Does any code call `player.performCommand(...)` or `Bukkit.dispatchCommand(...)` with a string segment a player could influence?
- Is the concatenated value guaranteed to be a server-controlled constant? Only flag when user influence is plausible.
- Could a third-party plugin override `Skill.getName()` or similar to inject attacker-controlled text?

**Permission Bypass**
- Does any new command handler or slot action skip a `player.hasPermission(...)` check?
- Does any admin-only operation use `player.isOp()` instead of a `mcrpg.*` node?
- Does `onClick()` return `false` without justification in a context where item movement would be harmful?
- Does any privileged action fail silently when permission is absent?

**SQL / Data Injection**
- Does any DAO method build a query with string concatenation instead of `PreparedStatement` parameters?
- Does any code deserialize player-provided bytes (NBT, base64, YAML) without validation?
- Does any `UpdateTableFunction` DDL use concatenated runtime values?

## Instructions

1. If no diff is in context, ask the user to paste the relevant diff or specify files.
2. Apply every checklist item to the changed code.
3. Organize findings by file. For each file with concerns, use this exact format:

### `path/to/File.java`

**[Issue title] — SEVERITY: HIGH / MEDIUM / LOW**
[One sentence describing the vulnerability and attack vector.]

```diff
- vulnerable line
+ corrected line
```

**AI Agent Prompt:** In `ClassName.java`, the `methodName()` method (around line N) [exact change needed, imports required, why safe for legitimate players]. ~150 words max.

---

1. If nothing to flag: "No security concerns found."
   Report only actual problems — no general style suggestions.
