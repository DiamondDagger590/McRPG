package us.eunoians.mcrpg.ability.stub;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * Stub combining {@link ActiveAbility}, {@link UnlockableAbility}, and
 * {@link ComboActivatable} for tests.
 */
public final class StubComboActivatableAbility extends StubAbilityBase implements UnlockableAbility, ActiveAbility, ComboActivatable {

    /**
     * @param plugin the McRPG plugin instance
     * @param name   the ability key name
     */
    public StubComboActivatableAbility(@NotNull McRPG plugin, @NotNull String name) {
        super(plugin, name);
    }

    @Override
    public int getUnlockLevel() {
        return 1;
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        return true;
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        return 0;
    }
}
