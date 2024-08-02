package us.eunoians.mcrpg.ability.attribute;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class serves as a manager for all {@link AbilityAttribute}s and serves as a hub for all plugins to hook into
 * to register their own unique attributes.
 * <p>
 * The code at this time isn't dynamic enough to fully support the desired 3rd party plugin implementation, however
 * this is the initial version of the system with the intent for the plugin recode to expand upon this.
 */
public class AbilityAttributeManager {

    public static final NamespacedKey ABILITY_COOLDOWN_ATTRIBUTE_KEY = new NamespacedKey(McRPG.getInstance(), "ability_cooldown_attribute");
    public static final NamespacedKey ABILITY_TIER_ATTRIBUTE_KEY = new NamespacedKey(McRPG.getInstance(), "ability_tier_attribute");
    public static final NamespacedKey ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY = new NamespacedKey(McRPG.getInstance(), "ability_toggled_off_attribute");
    public static final NamespacedKey ABILITY_UNLOCKED_ATTRIBUTE = new NamespacedKey(McRPG.getInstance(), "ability_unlocked_attribute");
    public static final NamespacedKey ABILITY_QUEST_ATTRIBUTE = new NamespacedKey(McRPG.getInstance(), "ability_quest_attribute");
    public static final NamespacedKey ABILITY_LOCATION_ATTRIBUTE = new NamespacedKey(McRPG.getInstance(), "ability_location_attribute");
    public static final NamespacedKey ABILITY_MATERIAL_SET_ATTRIBUTE = new NamespacedKey(McRPG.getInstance(), "ability_material_set_attribute");
    private final McRPG plugin;
    private final Map<String, NamespacedKey> abilityAttributeKeys;
    private final Map<NamespacedKey, AbilityAttribute<?>> abilityAttributes;

    public AbilityAttributeManager(@NotNull McRPG mcRPG) {
        this.plugin = mcRPG;
        this.abilityAttributeKeys = new HashMap<>();
        this.abilityAttributes = new HashMap<>();

        registerDefaultAttributes();
    }

    public void registerDefaultAttributes(){
        registerAttribute(new AbilityTierAttribute());
        registerAttribute(new AbilityCooldownAttribute());
        registerAttribute(new AbilityToggledOffAttribute());
        registerAttribute(new AbilityUnlockedAttribute());
        registerAttribute(new AbilityUpgradeQuestAttribute());
        registerAttribute(new AbilityLocationAttribute());
    }

    /**
     * Registers the provided {@link AbilityAttribute} using the {@link NamespacedKey} found from {@link AbilityAttribute#getNamespacedKey()}
     * to allow for easier lookups.
     *
     * @param abilityAttribute The {@link AbilityAttribute} to register
     */
    public void registerAttribute(@NotNull AbilityAttribute<?> abilityAttribute) {
        String key = abilityAttribute.getDatabaseKeyName();
        NamespacedKey namespacedKey = abilityAttribute.getNamespacedKey();

        abilityAttributeKeys.put(key, namespacedKey);
        abilityAttributes.put(namespacedKey, abilityAttribute);
    }

    /**
     * Checks to see if the provided {@link AbilityAttribute} is registered using the {@link NamespacedKey} found
     * via {@link AbilityAttribute#getNamespacedKey()}.
     *
     * @param abilityAttribute The {@link AbilityAttribute} to check
     * @return {@code true} if the provided {@link AbilityAttribute} is registered
     */
    public boolean isAttributeRegistered(@NotNull AbilityAttribute<?> abilityAttribute) {
        return isAttributeRegistered(abilityAttribute.getNamespacedKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} has an associated {@link AbilityAttribute} registered with it.
     *
     * @param namespacedKey The {@link NamespacedKey} to check
     * @return {@code true} if the provided {@link NamespacedKey} is registered
     */
    public boolean isAttributeRegistered(@NotNull NamespacedKey namespacedKey) {
        return abilityAttributes.containsKey(namespacedKey);
    }

    /**
     * Gets an {@link Optional} that contains the {@link AbilityAttribute} associated with the provided {@link String} database name
     * or an empty {@link Optional} if none are found.
     * <p>
     * The database name used is the {@link AbilityAttribute#getDatabaseKeyName()}, which is used to store the key/value pairs in the
     * {@link us.eunoians.mcrpg.database.table.SkillDAO}.
     *
     * @param attributeDatabaseName The {@link String} database name to use
     * @return An {@link Optional} containing the found {@link AbilityAttribute} or an empty {@link Optional} if no matches were found.
     */
    @NotNull
    public Optional<AbilityAttribute<?>> getAttribute(@NotNull String attributeDatabaseName) {
        NamespacedKey namespacedKey = abilityAttributeKeys.getOrDefault(attributeDatabaseName, null);
        return namespacedKey != null ? getAttribute(namespacedKey) : Optional.empty();
    }

    /**
     * Gets an {@link Optional} that contains the {@link AbilityAttribute} associated with the provided {@link NamespacedKey}
     * or an empty {@link Optional} if none are found.
     *
     * @param namespacedKey The {@link NamespacedKey} to use
     * @return An {@link Optional} containing the found {@link AbilityAttribute} or an empty {@link Optional} if no matches were found.
     */
    @NotNull
    public Optional<AbilityAttribute<?>> getAttribute(@NotNull NamespacedKey namespacedKey) {
        return Optional.ofNullable(abilityAttributes.get(namespacedKey));
    }
}
