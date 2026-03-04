# Add Skill

Scaffold a new McRPG skill end-to-end. Follow every step in order — do not skip steps.

---

## Step 0 — Gather inputs

Ask (or infer from context) before writing any code:

1. **Skill name** — PascalCase class name (e.g. `Archery`)
2. **Bukkit event that grants XP** — the specific event class (e.g. `EntityShootBowEvent`)
3. **HeldItemBonusSkill?** — does this skill grant a bonus based on which item is held? (`yes` / `no`)
4. **Namespace key string** — snake_case identifier (defaults to the lower-case skill name, e.g. `archery`)

---

## Step 1 — Read the canonical reference

Before writing any code, read the full `Swords` skill implementation:

- `src/main/java/us/eunoians/mcrpg/skill/impl/swords/Swords.java`
- `src/main/java/us/eunoians/mcrpg/skill/impl/swords/SwordsLevelOnAttackComponent.java`
- `src/main/java/us/eunoians/mcrpg/configuration/file/skill/SwordsConfigFile.java`

---

## Step 2 — Create the skill config file

Create `src/main/java/us/eunoians/mcrpg/configuration/file/skill/<Name>ConfigFile.java`:

```java
package us.eunoians.mcrpg.configuration.file.skill;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Contains all the {@link Route}s used for the <name>_configuration.yml
 */
public final class <Name>ConfigFile extends SkillConfigFile {

    private static final int CURRENT_VERSION = 1;

    public static final Route SKILL_ENABLED = Route.fromString("skill-enabled");
    // Add skill-specific route constants here

    @Override
    public @NotNull UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder()
                .setVersioning(new BasicVersioning("version"))
                .addCustomLogic(new HashMap<>())
                .addIgnoredRoutes(1, new HashSet<>())
                .build();
    }
}
```

---

## Step 3 — Add to FileType enum

In `src/main/java/us/eunoians/mcrpg/configuration/FileType.java`, add a new entry:

```java
<NAME>_CONFIG("skill_configuration" + "/" + "<name>_configuration.yml", new <Name>ConfigFile()),
```

Import the new config file class.

---

## Step 4 — Create the YAML config resource

Create `src/main/resources/skill_configuration/<name>_configuration.yml`:

```yaml
# <Name> Skill Configuration
version: 1

skill-enabled: true

# Maximum level for this skill
maximum-skill-level: 1000

# Level-up equation using Parser variables: {level}, {experience}
level-up-equation: "100 * {level}"

# Experience sources
experience:
  # List of item materials that grant XP for this skill
  allowed-items-for-experience-gain: []
  # Source-specific experience amounts (e.g. mob names/types)
  sources: {}
```

---

## Step 5 — Create the LevelableComponent

Create `src/main/java/us/eunoians/mcrpg/skill/impl/<name>/<Name>LevelComponent.java`.

Look at `SwordsLevelOnAttackComponent.java` to pick the correct parent class:
- For attack-based XP: extend `ConfigurableOnAttackLevelableComponent`
- For block-break XP: look at `MiningLevelOnBlockBreakComponent` as reference
- For other events: implement `LevelableComponent` directly

```java
package us.eunoians.mcrpg.skill.impl.<name>;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.<Name>ConfigFile;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

// Extend the appropriate parent
final class <Name>LevelComponent extends /* parent class */ {

    @Override
    public @NotNull YamlDocument getSkillConfiguration() {
        return RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.<NAME>_CONFIG);
    }

    @Override
    public @NotNull <Name> getSkill() {
        return (<Name>) RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.SKILL)
                .getRegisteredSkill(<Name>.<NAME_UPPER>_KEY);
    }
}
```

---

## Step 6 — Create the skill class

Create `src/main/java/us/eunoians/mcrpg/skill/impl/<name>/<Name>.java`:

```java
package us.eunoians.mcrpg.skill.impl.<name>;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.McRPGSkill;
import us.eunoians.mcrpg.skill.impl.type.ConfigurableSkill;
import us.eunoians.mcrpg.util.McRPGMethods;

// Add HeldItemBonusSkill if applicable: ", HeldItemBonusSkill"
public final class <Name> extends McRPGSkill implements ConfigurableSkill {

    public static final NamespacedKey <NAME_UPPER>_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "<name_key>");

    private final McRPG mcRPG;

    public <Name>(@NotNull McRPG mcRPG) {
        super(<NAME_UPPER>_KEY);
        this.mcRPG = mcRPG;
        addLevelableComponent(new <Name>LevelComponent(), <BukkitEvent>.class, 0);
    }

    @Override
    public @NotNull YamlDocument getYamlDocument() {
        return mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.<NAME>_CONFIG);
    }

    @Override
    public @NotNull Route getDisplayItemRoute() {
        return LocalizationKey.<NAME_UPPER>_DISPLAY_ITEM;
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return mcRPG;
    }

    @Override
    public @NotNull String getDatabaseName() {
        return "<name_key>";
    }
}
```

---

## Step 7 — Add the LocalizationKey display item route

In `src/main/java/us/eunoians/mcrpg/configuration/file/localization/LocalizationKey.java`:

1. Add a skill header constant (following the existing pattern, e.g. `SWORDS_HEADER`):
```java
private static final String <NAME_UPPER>_HEADER = toRoutePath(SKILLS_HEADER, "<name_key>");
```

2. Add the display item route:
```java
public static final Route <NAME_UPPER>_DISPLAY_ITEM = Route.fromString(toRoutePath(<NAME_UPPER>_HEADER, "display-item"));
```

---

## Step 8 — Add the locale file entry

In `src/main/resources/localization/english/en_skills.yml` (and any other bundled locale files), add a display item section for the new skill following the existing pattern for Swords, Mining, etc.

---

## Step 9 — Register in McRPGExpansion

In `src/main/java/us/eunoians/mcrpg/expansion/McRPGExpansion.java`, inside `getSkillContent()`:

```java
skillContent.addContent(new <Name>(mcRPG));
```

Import the new skill class.

---

## Step 10 — Register the FileManager

In the file manager (wherever `FileType` values are loaded into `YamlDocument` instances), ensure `<NAME>_CONFIG` is handled the same as the other skill configs. Check `src/main/java/us/eunoians/mcrpg/configuration/` for the `FileManager` class to see the loading pattern.

---

## Step 11 — Final verification

```
./gradlew compileJava
```

If compilation succeeds, run:

```
./gradlew test
```

Fix any failures before committing.

---

## Checklist

- [ ] `<Name>ConfigFile.java` created with `Route` constants
- [ ] `FileType.<NAME>_CONFIG` entry added
- [ ] YAML config file at `src/main/resources/skill_configuration/<name>_configuration.yml`
- [ ] `<Name>LevelComponent.java` created with correct parent
- [ ] `<Name>.java` created extending `McRPGSkill`, implementing `ConfigurableSkill`
- [ ] `LocalizationKey.<NAME_UPPER>_DISPLAY_ITEM` route added
- [ ] Locale YAML entry added for display item
- [ ] Registered in `McRPGExpansion.getSkillContent()`
- [ ] `./gradlew compileJava` passes
- [ ] `./gradlew test` passes
