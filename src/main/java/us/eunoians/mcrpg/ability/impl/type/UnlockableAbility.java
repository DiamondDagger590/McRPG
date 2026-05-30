package us.eunoians.mcrpg.ability.impl.type;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Set;

/**
 * An ability that must be unlocked before a holder can use it. Unlock eligibility is
 * expressed as a list of {@link UnlockConditionType}s combined with OR semantics — meeting
 * any condition makes the ability eligible to unlock. Compose AND-style requirements with
 * the {@code mcrpg:all_of} composite type.
 * <p>
 * The effective condition list comes from one of two sources:
 * <ul>
 *   <li>The ability's config — an {@code unlock-conditions} section under
 *       {@code ability-configuration.<ability-key>}. Server-owner-friendly.</li>
 *   <li>{@link #getDefaultUnlockConditions()} — the programmatic default the ability author
 *       ships. Used when config supplies no override.</li>
 * </ul>
 * <p>
 * Config-supplied conditions <i>replace</i> the Java default entirely (the
 * {@code UnlockConditionManager} logs a warning when this happens). The actual "is this
 * unlocked for this holder?" answer continues to read {@link AbilityUnlockedAttribute} via
 * {@link #isAbilityUnlocked(AbilityHolder)}: conditions describe eligibility, the attribute
 * records the achieved state.
 */
public interface UnlockableAbility extends Ability {

    /**
     * The programmatic default unlock conditions for this ability, used when the ability's
     * config declares no {@code unlock-conditions} section. The returned list has OR semantics
     * — meeting any condition makes the ability eligible. Defaults to empty; an ability with
     * neither a Java default nor config conditions is undiscoverable and surfaces a startup
     * warning from the {@code UnlockConditionManager}.
     *
     * @return the default conditions, never null (may be empty)
     */
    @NotNull
    default List<UnlockConditionType> getDefaultUnlockConditions() {
        return List.of();
    }

    /**
     * The effective unlock conditions: the config override if present, otherwise
     * {@link #getDefaultUnlockConditions()}. Resolved and cached by the
     * {@code UnlockConditionManager}.
     *
     * @return the effective conditions, never null (may be empty)
     */
    @NotNull
    default List<UnlockConditionType> getUnlockConditions() {
        return McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.UNLOCK_CONDITION)
                .resolve(this);
    }

    /**
     * Whether the given holder meets <i>any</i> unlock condition. Drives the skill level-up
     * unlock flow and the login-time unlock sweep. Display-only conditions
     * ({@code mcrpg:display_hint}) always report {@code false} and never auto-unlock.
     *
     * @param holder the holder to evaluate against
     * @return {@code true} if at least one condition is met
     */
    default boolean isAnyConditionMet(@NotNull AbilityHolder holder) {
        for (UnlockConditionType condition : getUnlockConditions()) {
            if (condition.isMet(holder)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    default Set<NamespacedKey> getApplicableAttributes() {
        return Set.of(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY,
                AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
    }

    /**
     * Whether this ability is currently unlocked for the holder. Reads the
     * {@link AbilityUnlockedAttribute} — the canonical source of truth.
     *
     * @param abilityHolder the holder to check
     * @return {@code true} if the holder's unlock attribute is set
     */
    default boolean isAbilityUnlocked(@NotNull AbilityHolder abilityHolder) {
        var abilityDataOptional = abilityHolder.getAbilityData(this);
        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            var attributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
            if (attributeOptional.isPresent() && attributeOptional.get() instanceof AbilityUnlockedAttribute attribute) {
                return attribute.getContent();
            }
        }
        return false;
    }
}
