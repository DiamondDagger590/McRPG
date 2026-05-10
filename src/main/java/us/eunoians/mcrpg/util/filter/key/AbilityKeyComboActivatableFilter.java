package us.eunoians.mcrpg.util.filter.key;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.Collection;

/**
 * A {@link McRPGPlayerContextFilter} that filters ability keys based on whether the
 * corresponding ability is a {@link ComboActivatable}.
 * <p>
 * Can be configured in either direction: keep only combo-activatable abilities (for populating
 * active ability select lists) or keep only non-combo-activatable abilities (for populating
 * passive ability select lists).
 */
public class AbilityKeyComboActivatableFilter implements McRPGPlayerContextFilter<NamespacedKey> {

    private final boolean comboActivatableOnly;

    /**
     * @param comboActivatableOnly {@code true} to retain only {@link ComboActivatable} abilities;
     *                             {@code false} to retain only non-{@link ComboActivatable} abilities.
     */
    public AbilityKeyComboActivatableFilter(boolean comboActivatableOnly) {
        this.comboActivatableOnly = comboActivatableOnly;
    }

    @Override
    public Collection<NamespacedKey> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<NamespacedKey> collection) {
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        return collection.stream()
                .filter(key -> {
                    boolean isComboActivatable = abilityRegistry.getRegisteredAbility(key) instanceof ComboActivatable;
                    return comboActivatableOnly == isComboActivatable;
                })
                .toList();
    }
}
