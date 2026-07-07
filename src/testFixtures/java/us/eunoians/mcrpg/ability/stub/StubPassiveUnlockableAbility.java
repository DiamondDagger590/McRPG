package us.eunoians.mcrpg.ability.stub;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;

/**
 * Stub combining {@link PassiveAbility} and {@link UnlockableAbility} for tests.
 */
public final class StubPassiveUnlockableAbility extends StubAbilityBase implements UnlockableAbility, PassiveAbility {

    /**
     * @param plugin the McRPG plugin instance
     * @param name   the ability key name
     */
    public StubPassiveUnlockableAbility(@NotNull McRPG plugin, @NotNull String name) {
        super(plugin, name);
    }

    @Override
    public int getUnlockLevel() {
        return 1;
    }
}
