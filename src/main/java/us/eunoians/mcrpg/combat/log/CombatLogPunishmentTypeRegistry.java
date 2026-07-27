package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of all registered {@link CombatLogPunishmentType} instances.
 * Supports lookup by {@link NamespacedKey} for deserialization of audit trail entries.
 * <p>
 * Built-in types are registered during {@code McRPGExpansion} content pack processing;
 * third-party types are added via {@link us.eunoians.mcrpg.expansion.content.CombatLogPunishmentContentPack}.
 */
public class CombatLogPunishmentTypeRegistry implements Registry<CombatLogPunishmentType> {

    private final Map<NamespacedKey, CombatLogPunishmentType> punishmentTypes = new LinkedHashMap<>();

    /**
     * Registers a {@link CombatLogPunishmentType}.
     *
     * @param punishmentType The punishment type to register.
     */
    @Override
    public void register(@NotNull CombatLogPunishmentType punishmentType) {
        punishmentTypes.put(punishmentType.getKey(), punishmentType);
    }

    /**
     * Looks up a {@link CombatLogPunishmentType} by its {@link NamespacedKey}.
     *
     * @param key The key to look up.
     * @return An {@link Optional} containing the type if registered.
     */
    @NotNull
    public Optional<CombatLogPunishmentType> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(punishmentTypes.get(key));
    }

    /**
     * Checks whether the given punishment type is registered, comparing by key.
     *
     * @param punishmentType The punishment type to check.
     * @return {@code true} if a type with the same key is registered.
     */
    @Override
    public boolean registered(@NotNull CombatLogPunishmentType punishmentType) {
        return punishmentTypes.containsKey(punishmentType.getKey());
    }
}
