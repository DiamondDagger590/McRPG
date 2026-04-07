# McRPG Web Quest Editor

## Overview

The McRPG Web Quest Editor is a browser-based visual editor for creating and modifying quest configurations. It follows the architecture pioneered by [LuckPerms' web editor](https://github.com/LuckPerms/LuckPermsWeb): a static single-page application (SPA) that communicates with the Minecraft plugin through two intermediary services — **bytebin** (HTTP content storage) and **bytesocks** (WebSocket relay). This decoupled design avoids direct browser-to-server connections, eliminating firewall and NAT issues that plague Minecraft server networking.

**Target audience:** Non-technical server owners who can install plugins and run commands but are uncomfortable editing YAML by hand. The editor prioritizes guided workflows, plain-English labels, and visual previews over raw config manipulation.

**Repository:** Separate from McRPG. The web editor lives in its own repository with independent CI/CD, deployed to Netlify as a static site.

---

## User Stories

These user stories drive the design of every feature in the editor. Each story is tagged with its implementation phase.

### Quest Editing (Phase 1)

| ID | Story | Acceptance Criteria |
|----|-------|-------------------|
| QE-1 | As a server owner, I want to run `/mcrpg editor` and get a link I can open in my browser to edit quests | Command generates a URL; opening it loads my server's current quest data |
| QE-2 | As a server owner, I want to edit an existing quest's objectives, rewards, and metadata through a visual form | Changes to any field are reflected in the editor state; no YAML knowledge required |
| QE-3 | As a server owner, I want to create a new quest using a step-by-step wizard that supports both simple (single-phase) and advanced (multi-phase) modes | Simple mode: basics → objectives → rewards → review → save. Advanced mode adds phase configuration and branching between steps 1 and 2. |
| QE-4 | As a server owner, I want to choose which YAML file a new quest is saved to, or create a new file | File picker shows existing files; "New File" option lets me name and place a new one |
| QE-5 | As a server owner, I want to delete a quest from the editor | Quest is removed from the editor state; on apply, removed from the YAML file |
| QE-6 | As a server owner, I want to see a preview of how my quest looks in the Minecraft GUI, toggling between rarity tiers if the quest supports multiple | Editor renders the quest board offering slot (per rarity), the active quest detail view, and the reward display. Rarity toggle shows how display material, name color, and scaling change per tier. |
| QE-7 | As a server owner, I want to review a summary of all my changes before applying them, then apply and have them take effect on my server | "Apply" opens a change review modal showing added/modified/deleted quests with a diff view. Confirm writes YAML and shows "Run `/mcrpg quest admin reload` to apply." |
| QE-8 | As a server owner, I want to export my in-progress work as YAML if my connection drops | "Export as YAML" button is always available; downloads a zip of all modified files |
| QE-9 | As a server owner, I want to **duplicate an existing quest** as a starting point for a new variant | "Duplicate" button on any quest creates a copy with a new key; opens it in the editor with all fields pre-filled |
| QE-10 | As a server owner, I want to **move a quest** from one YAML file to another | Right-click or menu option on a quest opens the file picker to reassign its target file |
| QE-11 | As a server owner, I want to **see the raw YAML preview** of a quest I'm editing | "View as YAML" toggle or side panel shows the generated YAML in real-time as fields change |

### Editor Experience (Phase 1)

| ID | Story | Acceptance Criteria |
|----|-------|-------------------|
| DX-1 | As a server owner, I want to **search and filter** my quest list by name, objective type, reward type, source file, or board eligibility | Search bar at top of quest list with type-ahead; filter dropdowns for objective type, reward type, and file |
| DX-2 | As a server owner, I want to **undo and redo changes** within my editing session | Ctrl+Z / Ctrl+Y (or on-screen buttons) step through an operation history. Applies to all editor actions (field edits, quest creation, deletion, reordering). |
| DX-3 | As a server owner, I want to **discard all changes** and revert to the server's current state | "Discard All Changes" button with confirmation dialog reloads the original payload |
| DX-4 | As a server owner, I want to **understand what each field means** via tooltips or inline help without leaving the editor | Every non-obvious field has an ⓘ icon that shows a plain-English explanation on hover/click. Examples: "Completion Mode: ALL means every stage must be completed. ANY means completing any single stage advances the quest." |
| DX-5 | As a server owner, I want to **understand what a reward type or objective type does** before selecting it | Dropdowns for objective types and reward types show a brief description beneath each option. Selecting one shows a detailed help panel explaining its config fields. |

### Board Configuration (Phase 1)

| ID | Story | Acceptance Criteria |
|----|-------|-------------------|
| BC-1 | As a server owner, I want to edit board rarity tiers (weights, multipliers, display settings) | Visual form with sliders for weights, numeric inputs for multipliers, material picker for display |
| BC-2 | As a server owner, I want to edit board categories (slot counts, visibility, refresh types) | Form shows all category fields with dropdowns for enums and numeric inputs for bounds |
| BC-3 | As a server owner, I want to create or delete board categories | "Add Category" and "Delete" buttons with confirmation dialogs |
| BC-4 | As a server owner, I want to edit rotation schedule (time, timezone, weekly reset day) | Timezone picker, time input, day-of-week dropdown |
| BC-5 | As a server owner, I want to **see which quests are eligible for each board category** and understand why ineligible quests are excluded | Category detail view shows a list of matching quests and a list of excluded quests with reasons (e.g., "Missing DAILY refresh type", "No matching rarity"). Shares matching logic with DG-2. |

### Configuration Diagnostics (Phase 1)

| ID | Story | Acceptance Criteria |
|----|-------|-------------------|
| DG-1 | As a server owner, I want to **see warnings when my quest configuration has logical issues** beyond schema errors | Warning panel highlights issues like: "Quest 'Deep Dive' has no supported rarities matching any category, so it will never appear on the board" or "Category 'land-daily' uses scope provider 'mcrpg:land' but no quests support the SCOPED visibility" |
| DG-2 | As a server owner, I want to **check if a specific quest can appear on the board** and understand exactly why or why not | Quest detail view has a "Board Eligibility" diagnostic section: lists each category and shows ✅ eligible / ❌ ineligible with specific reasons per category. Shares matching logic with BC-5. |

### Board Simulator (Phase 1.5)

| ID | Story | Acceptance Criteria |
|----|-------|-------------------|
| SIM-1 | As a server owner, I want to simulate N board rolls and see how often each quest appears | Simulator tab with roll count input; results show quest frequency distribution as a sortable table |
| SIM-2 | As a server owner, I want to simulate rolls for a specific **player profile** to test personal offerings | Player profile builder with: permission checkboxes, land membership toggles, skill levels, quest completion history, and active quest list. Simulator uses profile for personal offering generation and filtering. |
| SIM-3 | As a server owner, I want to see rarity distribution statistics across simulated rolls | Bar chart or table showing actual vs. expected rarity distribution with deviation highlighting |
| SIM-4 | As a server owner, I want to **tweak rarity weights or category settings in the simulator** and re-run without going back to the config editor | Simulator has inline override controls for key parameters; results show a comparison between current config and overridden values |

### Quest Templates (Phase 2 — Future)

| ID | Story | Acceptance Criteria |
|----|-------|-------------------|
| TMP-1 | As a server owner, I want to create quest templates using a visual builder | Drag-and-drop or form-based variable, phase, and objective creation; expression syntax hidden behind visual controls |
| TMP-2 | As a server owner, I want to preview generated quests from a template at each rarity tier | "Preview" button generates sample quests and shows them side-by-side by rarity |

---

## Architecture Overview

### System Diagram

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│                 │       │                 │       │                 │
│  McRPG Plugin   │◄─────►│    bytebin      │◄─────►│   Web Editor    │
│  (Minecraft     │ HTTP  │  (HTTP content  │ HTTP  │   (Vue 3 SPA    │
│   Server)       │       │   storage)      │       │    on Netlify)  │
│                 │       │                 │       │                 │
└────────┬────────┘       └─────────────────┘       └────────┬────────┘
         │                                                   │
         │                ┌─────────────────┐                │
         │                │                 │                │
         └───────────────►│   bytesocks     │◄───────────────┘
              WebSocket   │  (WebSocket     │   WebSocket
                          │   relay)        │
                          │                 │
                          └─────────────────┘
```

### Component Responsibilities

| Component | Role | Technology |
|-----------|------|-----------|
| **McRPG Plugin** | Serializes quest data to JSON, uploads to bytebin, maintains bytesocks session, receives edits, writes YAML to disk | Java 21, Paper API |
| **bytebin** | Temporary HTTP content storage. POST data → get key; GET key → retrieve data. Content auto-expires. | Java, Docker ([lucko/bytebin](https://github.com/lucko/bytebin)) |
| **bytesocks** | WebSocket channel relay. Clients join a channel by key; messages broadcast to all channel members. | Java, Docker ([lucko/bytesocks](https://github.com/lucko/bytesocks)) |
| **Web Editor** | Static SPA that renders quest data as visual forms, validates edits, and sends changes back through bytebin | Vue 3, TypeScript, Netlify |

### Data Flow — End to End

1. Server admin runs `/mcrpg editor` in-game
2. Plugin acquires a **session lock** (one editor session at a time)
3. Plugin serializes all quest definitions, board config, categories, and the **registry manifest** to a JSON payload
4. Plugin **gzip-compresses** the payload and uploads it to bytebin → receives a `payloadKey`
5. Plugin creates a bytesocks channel → receives a `channelKey`
6. Plugin constructs the editor URL: `https://editor.mcrpg.us/#{payloadKey}`
   - The `channelKey` and plugin's RSA public key are embedded in the bytebin payload
7. Plugin sends the URL to the admin as a clickable chat message
8. Admin opens the URL in their browser
9. Web editor fetches the payload from bytebin using the `payloadKey` from the URL fragment
10. Web editor decompresses the payload and renders the quest data as visual forms
11. Web editor connects to the bytesocks channel and performs the **RSA trust handshake** (see Security Model)
12. Admin makes edits in the browser
13. Admin clicks "Apply Changes" → editor shows a **change review modal** with a summary of all added/modified/deleted quests and a diff view (QE-7)
14. Admin reviews and clicks "Confirm"
15. Web editor compresses the change payload and uploads it to bytebin → receives a `changeKey`
16. Web editor sends the `changeKey` to the plugin via the bytesocks channel (signed message)
17. Plugin downloads the change payload from bytebin, validates it, and writes updated YAML files to disk
18. Plugin sends a confirmation back through bytesocks
19. Web editor shows: "Changes saved! Run `/mcrpg quest admin reload` to apply."
20. Plugin releases the session lock when the admin closes the editor or the session times out

---

## Security Model

Quest reward configurations can include arbitrary server commands (`mcrpg:command` reward type), making the editor session as security-sensitive as a permissions editor. The security model mirrors LuckPerms' RSA-signed WebSocket protocol.

### Threat Model

| Threat | Mitigation |
|--------|-----------|
| Attacker guesses bytebin URL and reads quest data | Bytebin keys are cryptographically random UUIDs; content auto-expires (configurable, default 15 minutes) |
| Attacker intercepts bytebin URL and injects malicious changes | All bytesocks messages are RSA-signed; plugin rejects unsigned or incorrectly signed change payloads |
| Man-in-the-middle on bytesocks channel | RSA trust handshake with nonce-based verification; admin must confirm new browser keys via in-game command |
| Replay attack (re-sending a previously valid change payload) | Each change payload includes a monotonic sequence number; plugin rejects out-of-order or duplicate sequences |
| Unauthorized player runs `/mcrpg editor` | Command requires `mcrpg.editor` permission (default: op-only) |
| Multiple simultaneous sessions cause conflicting writes | Single session lock; second `/mcrpg editor` invocation is rejected with "Editor already in use by {player}" |

### RSA Trust Handshake

This is the same protocol LuckPerms uses, adapted for McRPG:

```
Plugin                          bytesocks                        Web Editor
  │                                │                                │
  │──── create channel ───────────►│                                │
  │◄─── channelKey ───────────────│                                │
  │                                │                                │
  │   (URL sent to admin with payloadKey containing channelKey      │
  │    and plugin's RSA public key)                                 │
  │                                │                                │
  │                                │◄──── connect to channel ───────│
  │                                │                                │
  │◄───────── hello ──────────────│◄──── hello {nonce, editorPubKey}│
  │                                │                                │
  │  (Plugin checks if editorPubKey is trusted.                     │
  │   If NOT trusted: admin sees in-game prompt                     │
  │   "New editor connection from browser. Trust? [nonce]"          │
  │   Admin confirms → plugin stores editorPubKey as trusted)       │
  │                                │                                │
  │──── hello-reply {signed} ─────►│───── hello-reply ─────────────►│
  │                                │                                │
  │  (Session established. All subsequent messages are signed       │
  │   with sender's RSA private key and verified by recipient.)     │
```

### Message Envelope

All bytesocks messages use a JSON envelope with RSA signatures:

```json
{
    "msg": "{\"type\":\"change_applied\",\"payloadKey\":\"abc123\",\"seq\":1}",
    "signature": "BASE64_SHA256withRSA_SIGNATURE_OF_MSG_FIELD"
}
```

- `msg`: JSON-encoded packet (plaintext — signed, not encrypted)
- `signature`: SHA256withRSA signature of the `msg` string, using the sender's private key, base64-encoded

The recipient verifies the signature against the sender's public key (exchanged during the handshake). Messages with invalid or missing signatures are silently dropped.

### Session Lifecycle

| Event | Behavior |
|-------|----------|
| `/mcrpg editor` | Lock acquired, payload uploaded, URL sent to player |
| Browser opens URL | Payload downloaded, bytesocks handshake initiated |
| Trust confirmed | Full bidirectional communication enabled |
| "Apply Changes" clicked | Change payload uploaded, signed notification sent via bytesocks |
| Browser tab closed | Bytesocks `close` event → plugin releases lock after grace period (30s) |
| Player disconnects from server | Plugin sends `disconnect` via bytesocks → editor shows "Server disconnected" with export option |
| Server shuts down | Bytesocks connection drops → editor detects, shows export option |
| Session idle > 15 minutes | Plugin sends `timeout` via bytesocks → lock released, editor shows export option |
| `/mcrpg editor close` | Admin force-closes the active session; lock released immediately |

---

## Plugin-Side Implementation

### New Classes

```
us/eunoians/mcrpg/
├── editor/
│   ├── WebEditorManager.java          # Session lock, lifecycle management
│   ├── WebEditorSession.java          # Single session: keys, state, bytesocks connection
│   ├── WebEditorPayloadSerializer.java  # Quest data → JSON, JSON → YAML files
│   ├── WebEditorSecurityManager.java  # RSA key management, message signing/verification
│   ├── RegistryManifest.java          # Snapshot of all registered types for the editor
│   └── WebEditorSocket.java           # bytesocks WebSocket client wrapper
├── command/
│   └── editor/
│       ├── EditorCommand.java         # /mcrpg editor — opens session
│       ├── EditorCloseCommand.java    # /mcrpg editor close — force-closes session
│       └── EditorTrustCommand.java    # /mcrpg editor trust <nonce> — trusts a browser key
```

### WebEditorManager

Registered as a manager via `McRPGManagerKey.WEB_EDITOR` and accessed through `RegistryAccess`. Responsibilities:

- Holds the **active session** (at most one at a time)
- Manages the RSA keypair (generated once, persisted to `plugins/McRPG/editor-keypair.json`)
- Maintains a **trusted browser keys** list (persisted to `plugins/McRPG/editor-trusted-keys.json`)
- Handles session timeout scheduling via `DelayableCoreTask`

### Payload Structure

The initial payload uploaded to bytebin by the plugin:

```json
{
    "version": 1,
    "serverName": "My Server",
    "mcrpgVersion": "2.0.0-SNAPSHOT",
    "timestamp": 1712500000000,
    "channelKey": "abc123",
    "pluginPublicKey": "BASE64_RSA_PUBLIC_KEY",
    "manifest": { ... },
    "data": {
        "quests": { ... },
        "boardConfig": { ... },
        "categories": { ... }
    }
}
```

### Registry Manifest

The manifest tells the editor what types, skills, and entities exist on this server. It populates all dropdowns and validation rules in the editor.

```json
{
    "manifest": {
        "objectiveTypes": [
            {
                "key": "mcrpg:block_break",
                "displayName": "Break Blocks",
                "configSchema": {
                    "blocks": { "type": "block_list", "required": true }
                }
            },
            {
                "key": "mcrpg:mob_kill",
                "displayName": "Kill Mobs",
                "configSchema": {
                    "mobs": { "type": "entity_type_list", "required": true }
                }
            }
        ],
        "rewardTypes": [
            {
                "key": "mcrpg:experience",
                "displayName": "Skill Experience",
                "configSchema": {
                    "skill": { "type": "skill_ref", "required": true },
                    "amount": { "type": "positive_integer", "required": true }
                }
            },
            {
                "key": "mcrpg:command",
                "displayName": "Server Command",
                "configSchema": {
                    "command": { "type": "string", "required": true },
                    "description": { "type": "string", "required": false }
                }
            }
        ],
        "scopeProviders": [
            { "key": "mcrpg:single_player", "displayName": "Single Player" },
            { "key": "mcrpg:land", "displayName": "Land (Lands Plugin)" }
        ],
        "skills": [
            { "key": "mcrpg:swords", "displayName": "Swords" },
            { "key": "mcrpg:mining", "displayName": "Mining" },
            { "key": "mcrpg:herbalism", "displayName": "Herbalism" },
            { "key": "mcrpg:woodcutting", "displayName": "Woodcutting" }
        ],
        "entityTypes": ["ZOMBIE", "SKELETON", "SPIDER", "CREEPER", "..."],
        "blockTypes": ["STONE", "IRON_ORE", "DIAMOND_ORE", "OAK_LOG", "..."],
        "materials": ["DIAMOND_SWORD", "IRON_PICKAXE", "..."],
        "refreshTypes": ["DAILY", "WEEKLY"],
        "visibilityTypes": ["PERSONAL", "SHARED", "SCOPED"],
        "completionModes": ["ALL", "ANY"],
        "repeatModes": ["ONCE", "REPEATABLE", "COOLDOWN", "LIMITED", "COOLDOWN_LIMITED"],
        "questStates": ["NOT_STARTED", "IN_PROGRESS", "COMPLETED", "CANCELLED"],
        "installedExpansions": ["mcrpg:core"]
    }
}
```

**Key design decision:** Each objective type and reward type includes a `configSchema` that describes what fields its `config` section expects. This allows the editor to dynamically render the correct form fields for any objective or reward type — including third-party types registered by expansions. The schema uses simple type descriptors (`block_list`, `entity_type_list`, `skill_ref`, `positive_integer`, `string`, `duration`) that the editor maps to appropriate UI widgets.

### Quest Data Structure

Each quest is serialized with its **source file path** so the editor knows where to write it back:

```json
{
    "quests": {
        "mcrpg:zombie_slayer": {
            "sourceFile": "quest-board/quests/daily/combat.yml",
            "definition": {
                "scope": "mcrpg:single_player",
                "expiration": "24h",
                "repeatMode": "ONCE",
                "display": {
                    "name": "Zombie Slayer",
                    "description": "Clear the undead menace",
                    "objectives": { "kill_zombies": "Slay zombies" },
                    "rewards": { "swords_xp": "Swords Experience" }
                },
                "boardMetadata": {
                    "boardEligible": true,
                    "supportedRarities": ["mcrpg:common", "mcrpg:uncommon"],
                    "supportedRefreshTypes": ["DAILY"]
                },
                "phases": {
                    "hunt": {
                        "completionMode": "ALL",
                        "stages": {
                            "slay": {
                                "objectives": {
                                    "kill_zombies": {
                                        "type": "mcrpg:mob_kill",
                                        "requiredProgress": 20,
                                        "config": {
                                            "mobs": ["ZOMBIE", "HUSK", "DROWNED"]
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                "rewards": {
                    "swords_xp": {
                        "type": "mcrpg:experience",
                        "skill": "SWORDS",
                        "amount": 2500
                    }
                }
            }
        }
    }
}
```

### YAML File Targeting

When the plugin receives changes back from the editor, it must write quests to the correct YAML files. The rules are:

1. **Existing quests**: Written back to their `sourceFile` path (tracked in the payload)
2. **New quests**: The editor includes a `targetFile` path chosen by the user
3. **Deleted quests**: Removed from their `sourceFile`; if the file becomes empty, it is deleted
4. **New files**: If `targetFile` references a file that doesn't exist, the plugin creates it with the standard `quests:` top-level key

The change payload sent from the editor includes an explicit changeset:

```json
{
    "version": 1,
    "seq": 1,
    "changes": {
        "quests": {
            "added": {
                "mcrpg:new_quest": {
                    "targetFile": "quest-board/quests/daily/my-custom-quests.yml",
                    "definition": { ... }
                }
            },
            "modified": {
                "mcrpg:zombie_slayer": {
                    "sourceFile": "quest-board/quests/daily/combat.yml",
                    "definition": { ... }
                }
            },
            "removed": ["mcrpg:old_quest"]
        },
        "categories": {
            "added": { ... },
            "modified": { ... },
            "removed": [...]
        },
        "boardConfig": {
            "modified": { ... }
        }
    }
}
```

### Payload Size Considerations

Large servers may have hundreds of quests across dozens of files. Mitigations:

- **gzip compression**: All bytebin payloads are gzip-compressed (typically 80-90% size reduction for JSON)
- **Bytebin size limit**: Configure a max payload size (default 1MB compressed; sufficient for ~1000+ quests)
- **Lazy loading (future)**: If payloads grow beyond practical limits, the editor could request quest data per-file rather than all at once. This is not needed for Phase 1.

---

## Backend Services

### Deployment Architecture

Both bytebin and bytesocks are deployed as Docker containers behind an nginx reverse proxy on a single VPS.

```
┌──────────────────────────────────────────────┐
│  VPS (e.g. Hetzner CX22 — ~$4/month)        │
│                                              │
│  ┌──────────────────────────────────────┐    │
│  │  nginx (reverse proxy + TLS)         │    │
│  │  ├── api.mcrpg.us/bin/* → bytebin    │    │
│  │  └── api.mcrpg.us/sock/* → bytesocks │    │
│  └──────────────────────────────────────┘    │
│                                              │
│  ┌────────────┐       ┌─────────────┐        │
│  │  bytebin   │       │  bytesocks  │        │
│  │  :8080     │       │  :8081      │        │
│  └────────────┘       └─────────────┘        │
│                                              │
│  Docker Compose manages both services        │
└──────────────────────────────────────────────┘
```

### Domain Structure

| URL | Service | Purpose |
|-----|---------|---------|
| `editor.mcrpg.us` | Netlify (web editor SPA) | The browser-facing editor application |
| `api.mcrpg.us/bin/` | bytebin | Content storage (payload upload/download) |
| `api.mcrpg.us/sock/` | bytesocks | WebSocket relay channels |

Using a single API subdomain with path-based routing simplifies CORS configuration and SSL certificate management.

### bytebin Configuration

Key settings for the McRPG deployment:

```yaml
# bytebin config
host: "0.0.0.0"
port: 8080
keyLength: 12
lifetimeMinutes: 30          # Payloads expire after 30 minutes
maxContentLengthMb: 2        # Max 2MB per payload (compressed)
cors:
  allowedOrigins:
    - "https://editor.mcrpg.us"
```

### bytesocks Configuration

```yaml
# bytesocks config
host: "0.0.0.0"
port: 8081
keyLength: 16
channelLifetimeMinutes: 60   # Channels expire after 60 minutes of inactivity
cors:
  allowedOrigins:
    - "https://editor.mcrpg.us"
```

### Operational Concerns

| Concern | Mitigation |
|---------|-----------|
| **Service availability** | Docker restart policies (`unless-stopped`); simple uptime monitoring (e.g. UptimeRobot free tier) |
| **Rate limiting** | nginx `limit_req` module: 10 req/s per IP for bytebin, 2 channel creates/min per IP for bytesocks |
| **Abuse prevention** | bytebin payload expiry + max size; bytesocks channel expiry; CORS origin restriction |
| **Backups** | No persistent data to back up — bytebin is ephemeral, bytesocks is stateless |
| **Updates** | Pull new Docker images, `docker compose up -d` — zero-downtime for independent services |
| **Cost** | ~$4-6/month for a small VPS. Bytebin and bytesocks are extremely lightweight; a single CX22 handles thousands of concurrent sessions |

---

## Web Editor Frontend

### Technology Stack

| Technology | Purpose | Why This Choice |
|-----------|---------|----------------|
| **Vue 3** | UI framework | Gentle learning curve; template syntax is intuitive for frontend newcomers; LuckPerms uses Vue so reference code exists in the same domain |
| **TypeScript** | Type safety | Catches errors at compile time; IDE autocompletion for quest data structures; worth the learning investment |
| **Vite** | Build tool | Fast dev server with hot module replacement; simpler config than Webpack; Vue 3's recommended build tool |
| **Pinia** | State management | Vue 3's official state manager (replaces Vuex); simpler API, TypeScript-first |
| **pako** | gzip compression | JavaScript gzip library for compressing/decompressing bytebin payloads; same library LuckPerms uses |
| **Vue Router** | Client-side routing | SPA routing between editor tabs (Quests, Board Config, Categories, Simulator) |

### Frontend Concepts Primer

For someone unfamiliar with frontend engineering, here are the key concepts this design relies on:

**Single-Page Application (SPA):** The entire editor is one HTML page. When you click between "Quests" and "Board Config", the browser doesn't reload — JavaScript swaps out the visible content. This is what Vue Router handles. The server (Netlify) always serves the same `index.html`; the URL fragment (`#/quests`, `#/board-config`) tells Vue which view to render.

**Components:** Vue breaks the UI into reusable pieces called components. Each component is a `.vue` file containing three sections: `<template>` (HTML), `<script>` (JavaScript/TypeScript logic), and `<style>` (CSS). Components nest inside each other like HTML elements. For example, `QuestEditor.vue` contains multiple `ObjectiveEditor.vue` instances.

**Reactive State:** Vue automatically re-renders the UI when data changes. If you update a quest's name in the Pinia store, every component displaying that name updates instantly. You don't manually manipulate the DOM (the HTML elements on the page).

**Props and Events:** Parent components pass data down to children via "props" (like function arguments). Children communicate back up via "events" (like callbacks). This one-way data flow makes the application predictable.

### Application Structure

```
mcrpg-web-editor/
├── public/
│   ├── index.html
│   └── assets/
│       └── minecraft-sprites/          # Vanilla item/block sprite atlas for previews
├── src/
│   ├── App.vue                         # Root component (layout, nav bar, connection status)
│   ├── main.ts                         # Entry point (Vue app initialization)
│   ├── router/
│   │   └── index.ts                    # Route definitions (Quests, Board Config, etc.)
│   ├── stores/                         # Pinia state stores
│   │   ├── session.ts                  # Connection state, bytebin/bytesocks management
│   │   ├── quests.ts                   # Quest definitions, change tracking
│   │   ├── boardConfig.ts              # Rarity, rotation, notification config
│   │   ├── categories.ts              # Board category definitions
│   │   └── manifest.ts                # Registry manifest (objective types, reward types, etc.)
│   ├── services/                       # Non-UI logic
│   │   ├── bytebin.ts                  # HTTP client for bytebin (upload/download/compress)
│   │   ├── bytesocks.ts               # WebSocket client for bytesocks
│   │   ├── crypto.ts                   # RSA key generation, signing, verification
│   │   └── yaml-export.ts             # Emergency YAML export (uses js-yaml library)
│   ├── components/                     # Reusable UI components
│   │   ├── common/
│   │   │   ├── NamespacedKeyInput.vue  # Input for "namespace:key" values with validation
│   │   │   ├── DurationInput.vue       # Human-friendly duration input (1d 6h 30m)
│   │   │   ├── MaterialPicker.vue      # Searchable Minecraft material dropdown with sprite preview
│   │   │   ├── EntityTypePicker.vue    # Searchable entity type dropdown
│   │   │   ├── BlockTypePicker.vue     # Searchable block type dropdown
│   │   │   ├── EnumDropdown.vue        # Generic dropdown for enum values (scope, visibility, etc.)
│   │   │   ├── ConfirmDialog.vue       # Reusable confirmation modal
│   │   │   └── ConnectionStatus.vue    # Shows "Connected" / "Disconnected" / "Reconnecting"
│   │   ├── quest/
│   │   │   ├── QuestList.vue           # Sidebar list of all quests, grouped by file
│   │   │   ├── QuestEditor.vue         # Main quest editing form
│   │   │   ├── QuestWizard.vue         # Step-by-step new quest creation
│   │   │   ├── PhaseEditor.vue         # Phase editing (completion mode, stage list)
│   │   │   ├── StageEditor.vue         # Stage editing (objective list)
│   │   │   ├── ObjectiveEditor.vue     # Objective editing (type-driven dynamic form)
│   │   │   ├── RewardEditor.vue        # Reward editing (type-driven dynamic form)
│   │   │   ├── BoardMetadataEditor.vue # Board eligibility, rarities, refresh types
│   │   │   ├── QuestPreview.vue        # In-game GUI approximation
│   │   │   └── FilePicker.vue          # YAML file selection / creation for new quests
│   │   ├── board/
│   │   │   ├── RarityEditor.vue        # Rarity tier editing (weight, multipliers, display)
│   │   │   ├── CategoryEditor.vue      # Board category editing
│   │   │   ├── CategoryList.vue        # List of all categories
│   │   │   └── RotationEditor.vue      # Rotation schedule editing
│   │   └── simulator/                  # Phase 1.5
│   │       ├── BoardSimulator.vue      # Main simulator view
│   │       ├── PlayerProfileBuilder.vue # Mock player profile configuration
│   │       └── SimulationResults.vue   # Frequency/distribution charts
│   ├── views/                          # Page-level components (one per route)
│   │   ├── QuestEditorView.vue         # Quest editing page (QuestList + QuestEditor)
│   │   ├── BoardConfigView.vue         # Board config page (Rarities + Rotation)
│   │   ├── CategoryView.vue            # Category management page
│   │   ├── SimulatorView.vue           # Board simulator page (Phase 1.5)
│   │   └── LoadingView.vue             # Shown while payload is downloading
│   └── types/                          # TypeScript type definitions
│       ├── quest.ts                    # Quest, Phase, Stage, Objective, Reward types
│       ├── board.ts                    # Rarity, Category, Rotation types
│       ├── manifest.ts                # Registry manifest types
│       ├── session.ts                 # Session, message envelope types
│       └── changes.ts                 # Changeset types (added/modified/removed)
├── package.json
├── vite.config.ts
├── tsconfig.json
└── netlify.toml                        # Netlify build config + SPA redirect rules
```

### UI Layout

The editor uses a standard dashboard layout:

```
┌─────────────────────────────────────────────────────────────────┐
│  McRPG Quest Editor          [Connected ●]     [Apply Changes]  │
├──────────┬──────────────────────────────────────────────────────┤
│          │                                                      │
│  NAV     │   CONTENT AREA                                       │
│          │                                                      │
│  Quests  │   (Quest editor form, board config form, etc.)       │
│  Board   │                                                      │
│  ◦Config │                                                      │
│  ◦Categs │                                                      │
│  Simulate│                                                      │
│          │                                                      │
│  ──────  │                                                      │
│  Export  │                                                      │
│          │                                                      │
└──────────┴──────────────────────────────────────────────────────┘
```

### Quest Editor UX Flow

The quest list (left sidebar when on the Quests tab) groups quests by their source file:

```
📁 quest-board/quests/daily/combat.yml
   ├── 🗡️ Zombie Slayer
   ├── 🗡️ Skeleton Hunter
   └── 🗡️ Cave Spider Purge
📁 quest-board/quests/weekly/mining.yml
   ├── ⛏️ Deep Dive
   └── ⛏️ Diamond Rush
📁 quest/upgrade_quests.yml
   └── ⬆️ Bleed Tier II

[+ New Quest]
```

Clicking a quest loads it into the content area as a form. The form is organized into collapsible sections:

1. **Basics** — Key (read-only for existing, editable for new), scope dropdown, expiration, repeat mode
2. **Display** — Name, description, objective labels, reward labels
3. **Board Settings** — Eligible toggle, supported rarities (checkboxes), supported refresh types
4. **Phases** — Expandable phase cards, each containing stages, each containing objectives
5. **Rewards** — Reward cards with type-driven dynamic forms
6. **Preview** — Live approximation of the in-game GUI appearance

### New Quest Wizard

Creating a new quest uses a multi-step wizard (QE-3) to avoid overwhelming the user. The wizard supports two modes:

- **Simple mode** (default): Creates a single-phase, single-stage quest — covers ~90% of use cases
- **Advanced mode**: Unlocked via a toggle; allows multi-phase configuration with branching and per-stage rewards

The wizard can also be entered via the **Duplicate** action (QE-9), which pre-fills all fields from the source quest and focuses the user on changing the key and the fields they want to differ.

#### Simple Mode Steps

**Step 1 — Basics:**
- "What should this quest be called?" → text input for display name
- "Who can do this quest?" → dropdown (Single Player / Land Group / etc.) with ⓘ tooltip explaining each option (DX-4)
- "Does this quest expire?" → toggle + duration input
- "Can this quest be repeated?" → dropdown (Once / Repeatable / Cooldown / etc.) with ⓘ tooltip
- [Switch to Advanced Mode] link at the bottom

**Step 2 — Objectives:**
- "What do players need to do?" → [+ Add Objective] button
- Each objective: type dropdown (with descriptions per DX-5) → dynamic form based on type
  - "Break Blocks" → block type picker + count input
  - "Kill Mobs" → entity type picker + count input
  - etc.
- "Should objectives be completed in order or all at once?" → ALL / ANY toggle with ⓘ tooltip

**Step 3 — Rewards:**
- "What do players earn?" → [+ Add Reward] button
- Each reward: type dropdown (with descriptions per DX-5) → dynamic form
  - "Skill Experience" → skill dropdown + amount input
  - "Server Command" → command text input + description

**Step 4 — Board Settings:**
- "Should this quest appear on the quest board?" → toggle
- If yes: rarity checkboxes, refresh type checkboxes
- Board eligibility diagnostic (DG-2) shown inline: "This quest will be eligible for: personal-daily, shared-daily"

**Step 5 — Save Location:**
- "Where should this quest be saved?" → file picker
  - Shows existing YAML files grouped by directory
  - "Create new file" option → directory picker + file name input

**Step 6 — Review:**
- Summary of all choices + in-game preview (QE-6) with rarity toggle
- Raw YAML preview tab (QE-11)
- [Create Quest] button

#### Advanced Mode Additional Steps

When advanced mode is toggled on, Step 2 is replaced with a multi-phase editor:

**Step 2a — Phases:**
- "How many phases does this quest have?" → [+ Add Phase] button
- Each phase card: name, completion mode (ALL / ANY) with ⓘ tooltip
- Phases are ordered vertically; drag handles for reordering

**Step 2b — Stages & Objectives (per phase):**
- Each phase expands to show its stages
- Each stage contains its objectives (same UI as Simple mode Step 2)
- [+ Add Stage] button per phase

### Dynamic Form Rendering

Objective and reward forms are rendered dynamically based on the `configSchema` from the registry manifest. This is the key extensibility mechanism — third-party objective types work automatically if they provide a schema.

Schema type → UI widget mapping:

| Schema Type | Widget | Example |
|-------------|--------|---------|
| `string` | Text input | Command string |
| `positive_integer` | Number input (min=1) | Required progress |
| `positive_decimal` | Number input (min=0, step=0.01) | Damage amount |
| `boolean` | Toggle switch | Include baby variants |
| `duration` | DurationInput component | Expiration time |
| `block_list` | BlockTypePicker (multi-select) | Target blocks |
| `entity_type_list` | EntityTypePicker (multi-select) | Target mobs |
| `skill_ref` | Skill dropdown | Reward skill |
| `material_ref` | MaterialPicker | Display material |
| `namespaced_key` | NamespacedKeyInput | Custom reference |

### In-Game Preview (QE-6)

The preview component renders an approximation of how the quest appears in Minecraft:

- **Board offering slot**: Minecraft inventory slot with the rarity-appropriate material, colored name, and lore lines showing objectives and rewards
- **Quest detail view**: Multi-slot layout showing phase progression, objective descriptions with progress bars, and reward list
- **Rarity toggle**: If the quest supports multiple rarities, a dropdown or tab bar lets the user switch between them to see how the display material, name color, difficulty scaling, and reward multipliers change per tier

Rendering uses a canvas element (or CSS-based Minecraft font rendering) with vanilla Minecraft item sprites. For custom resource pack items (custom model data), the preview shows the base material with a badge: "Custom texture — preview unavailable".

The sprite atlas for vanilla items is shipped as a static asset in the web editor repo. It covers all standard Minecraft materials and entity types. This atlas must be updated when Minecraft adds new content, but only on major version bumps.

### Validation

The editor validates quest data in real-time as the user edits:

| Rule | When Checked | Error Display |
|------|-------------|---------------|
| Quest key must be unique | On key input | Inline error under the key field |
| Quest key must be valid namespaced key format | On key input | Inline error |
| At least one phase required | On save/apply | Section-level warning |
| At least one objective per stage | On save/apply | Section-level warning |
| Required progress must be > 0 | On input | Inline error |
| Reward amount must be > 0 | On input | Inline error |
| Objective type must be a registered type | Always (from manifest) | Dropdown prevents invalid selection |
| Board-eligible quests must have at least one supported rarity | On save/apply | Section-level warning |
| File path must be within allowed directories | On file selection | File picker prevents invalid selection |
| Duration format must be valid | On input | Inline error with format hint |

Validation errors prevent "Apply Changes" but allow "Export as YAML" (so users can save work-in-progress even if incomplete).

---

## Board Simulator (Phase 1.5)

The board simulator is a separate tab in the editor that lets server owners test their quest board configuration without running the server. It re-implements the board generation logic in JavaScript/TypeScript, using the same quest pool, rarity weights, and category rules from the loaded configuration.

### How It Works

1. User navigates to the "Simulator" tab
2. User configures a **player profile** (optional):
   - Permissions (checkboxes for each `required-permission` used by categories)
   - Land membership (for scoped categories)
   - Previous quest completions (to test cooldown/repeat behavior)
3. User sets **simulation parameters**:
   - Number of rolls (default: 100)
   - Rotation type to simulate (Daily / Weekly / Both)
4. User clicks "Run Simulation"
5. The simulator runs N independent board generations using the configured quests, templates (Phase 2), categories, and rarity weights
6. Results are displayed as:
   - **Quest frequency table**: How many times each quest appeared across all rolls, sorted by frequency
   - **Rarity distribution chart**: Bar chart comparing actual rarity distribution vs. expected (from configured weights)
   - **Category fill rate**: How often each category's slots were filled vs. empty (based on `chance-per-slot`)
   - **Anomaly warnings**: Quests that never appeared, rarities with 0 rolls, categories that always generated empty slots

### Implementation Notes

The simulator must faithfully replicate McRPG's `SlotGenerationLogic` and `QuestPool` behavior. This means:
- Porting the weighted random selection algorithm to TypeScript
- Respecting category min/max slots, priority ordering, and `chance-per-slot`
- Applying `appearance-cooldown` across simulated rotations
- Filtering quests by `supportedRarities` and `supportedRefreshTypes`

This is a non-trivial porting effort, but the logic is pure math with no Bukkit dependencies, making it straightforward to translate. The TypeScript implementation should be tested against known outputs from the Java implementation to ensure correctness.

---

## Error Handling & Recovery

### Connection Failure Scenarios

| Scenario | Editor Behavior |
|----------|----------------|
| bytebin unreachable on initial load | "Unable to load quest data. The McRPG services may be down. Please try again later." with a retry button |
| bytesocks connection drops mid-session | Banner: "Connection lost — reconnecting..." with automatic retry (exponential backoff: 1s, 2s, 4s, 8s, max 30s). Editing remains functional. |
| bytesocks reconnection fails after 5 attempts | Banner: "Connection lost. Your changes are safe locally." + prominent "Export as YAML" button |
| bytebin upload fails on "Apply Changes" | Toast: "Failed to save changes. Retrying..." with automatic retry. After 3 failures: "Unable to save. Export your changes as YAML and apply them manually." |
| Plugin sends `disconnect` message | Modal: "The server has disconnected (player left / server stopping). Export your changes to avoid losing work." |
| Plugin sends `timeout` message | Modal: "Session timed out due to inactivity. Export your changes and run `/mcrpg editor` again to start a new session." |

### Data Safety Guarantees

- The editor **never discards unsaved changes** without explicit user action
- All editor state is stored in the browser's memory (Pinia store) and persists across connection drops
- The "Export as YAML" button is **always available**, regardless of connection state
- When applying changes, the editor waits for the plugin's confirmation message before showing success
- If the browser tab is accidentally closed with unsaved changes, the browser's `beforeunload` event shows a confirmation dialog

---

## Implementation Phases

### Phase 1 — Core Quest Editor

**Goal:** Server owners can create, edit, and delete hand-crafted quests and board configuration through a visual web editor.

**Plugin-side work:**
1. Implement `WebEditorManager`, `WebEditorSession`, `WebEditorSocket`
2. Implement `WebEditorSecurityManager` (RSA keypair generation, signing, trust management)
3. Implement `WebEditorPayloadSerializer` (quest data → JSON, JSON → YAML round-trip)
4. Implement `RegistryManifest` (snapshot all registered types with config schemas)
5. Implement `/mcrpg editor`, `/mcrpg editor close`, `/mcrpg editor trust` commands
6. Add `mcrpg.editor` permission node
7. Add HTTP client for bytebin (POST/GET with gzip)
8. Add WebSocket client for bytesocks

**Infrastructure work:**
1. Provision VPS, install Docker
2. Deploy bytebin + bytesocks via Docker Compose
3. Configure nginx reverse proxy with TLS (Let's Encrypt)
4. Set up domain DNS (`api.mcrpg.us`, `editor.mcrpg.us`)
5. Configure Netlify deployment for the web editor repo

**Frontend work:**
1. Scaffold Vue 3 + TypeScript + Vite project
2. Implement bytebin and bytesocks service clients
3. Implement RSA crypto service (Web Crypto API)
4. Implement session store (connection lifecycle, trust handshake)
5. Implement manifest store and common picker components (MaterialPicker, EntityTypePicker, BlockTypePicker, etc.)
6. Implement quest store with change tracking and undo/redo history (DX-2)
7. Implement QuestList with search and filter (DX-1), grouped by source file
8. Implement QuestEditor and all sub-editors (Phase, Stage, Objective, Reward)
9. Implement QuestWizard with simple/advanced modes (QE-3) and duplicate entry point (QE-9)
10. Implement FilePicker for YAML file targeting and quest move (QE-10)
11. Implement QuestPreview with rarity toggle (QE-6)
12. Implement raw YAML preview panel (QE-11)
13. Implement board config store and editors (RarityEditor, CategoryEditor, RotationEditor)
14. Implement category → quest eligibility view (BC-5)
15. Implement contextual help tooltips and type descriptions (DX-4, DX-5)
16. Implement configuration diagnostics: logical warnings (DG-1) and per-quest board eligibility diagnostic (DG-2)
17. Implement validation layer (schema validation + logical warnings)
18. Implement change review modal with diff view (QE-7)
19. Implement "Apply Changes" flow (review → compress → upload → notify → confirm)
20. Implement "Export as YAML" emergency export (QE-8)
21. Implement "Discard All Changes" with confirmation (DX-3)
22. Implement error handling and reconnection logic
23. Implement ConnectionStatus indicator

**Estimated complexity:** Large. This is a full-stack project spanning three codebases (plugin, infrastructure, frontend). The plugin-side work is moderate (mostly serialization and HTTP/WebSocket plumbing). The infrastructure is a one-time setup. The frontend is the bulk of the effort.

### Phase 1.5 — Board Simulator

**Goal:** Server owners can simulate quest board rolls to validate their configuration, including personal offerings.

**Frontend work:**
1. Port `SlotGenerationLogic` and `QuestPool` weighted selection to TypeScript
2. Implement `PlayerProfileBuilder` component (permissions, land membership, skill levels, quest history, active quests) (SIM-2)
3. Implement `BoardSimulator` runner with support for both shared and personal offering generation
4. Implement `SimulationResults` with frequency tables, rarity distribution charts, and category fill rates (SIM-1, SIM-3)
5. Implement inline parameter override controls for A/B comparison (SIM-4)
6. Validate TypeScript simulation output against Java implementation for correctness

**Plugin-side work:** None — the simulator runs entirely in the browser using the already-loaded configuration data.

### Phase 2 — Quest Template Editor (Future)

**Goal:** Server owners can create and edit quest templates with a visual builder that hides the expression syntax.

**Key challenges:**
- Visual expression builder (replacing raw `kill_count * 20` syntax)
- Variable pool configuration (pool weights per rarity)
- Condition builder for template gating
- Template preview at each rarity tier (requires running the template engine in the browser)

This phase is intentionally left under-specified. The design should be informed by user feedback from Phase 1 and a deeper analysis of the template system's complexity.

### Phase 2+ — Future Considerations

- **Localization editing**: Edit locale YAML files through the editor
- **Version history**: Track changes over time, allow rollback
- **Collaborative editing**: Multiple admins editing simultaneously with conflict resolution
- **Self-hosted editor**: Docker Compose bundle that includes the web editor, bytebin, and bytesocks for privacy-conscious servers
- **Import/export profiles**: Save and share quest configurations between servers
- **AI-assisted quest generation**: Use LLM to suggest quest ideas based on existing content

---

## Key Design Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Data transfer model | Plugin-push via bytebin + bytesocks (LuckPerms model) | Best UX — zero file management for server owners; editor needs server-specific registry data |
| Standalone/offline mode | Not supported (export-only as error recovery) | Editor requires server registry data for validation; standalone mode would be severely limited |
| Security model | RSA-signed messages (same as LuckPerms) | Quest rewards can execute arbitrary commands — same threat level as permissions editing |
| Session concurrency | Single session lock | Prevents conflicting writes; simple to implement; adequate for Phase 1 |
| Change application | Write to disk + manual reload command | Avoids complex live migration of in-flight quests; reload is already safe |
| Frontend framework | Vue 3 + TypeScript + Vite | Gentle learning curve; LuckPerms reference code available; excellent documentation |
| State management | Pinia | Vue 3's official solution; TypeScript-first; simpler than Vuex |
| Hosting | Netlify (frontend) + self-hosted VPS (bytebin + bytesocks) | Free frontend hosting with PR previews; minimal backend cost (~$5/month) |
| Repository | Separate from McRPG | Clean separation of Java and TypeScript tooling; independent deployment |
| YAML serialization | Plugin handles all YAML ↔ JSON conversion | Avoids JavaScript YAML serialization pitfalls; plugin owns the canonical format |
| Payload format | gzip-compressed JSON | Efficient transfer; pako library handles compression in browser |
| In-game preview | Vanilla sprites only; custom resource pack items show placeholder | Honest UX; avoids requiring resource pack upload |
| Board simulator location | Same app, separate tab | Shares configuration data; no additional infrastructure needed |
| Template editor | Deferred to Phase 2 | Templates are complex; Phase 1 feedback should inform the visual builder design |

