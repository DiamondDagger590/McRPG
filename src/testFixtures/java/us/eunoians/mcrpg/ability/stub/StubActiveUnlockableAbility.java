package us.eunoians.mcrpg.ability.stub;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;

/**
 * Stub combining {@link ActiveAbility} and {@link UnlockableAbility} for tests.
 */
public final class StubActiveUnlockableAbility extends StubAbilityBase implements UnlockableAbility, ActiveAbility {

    /**
     * @param plugin the McRPG plugin instance
     * @param name   the ability key name
     */
    public StubActiveUnlockableAbility(@NotNull McRPG plugin, @NotNull String name) {
        super(plugin, name);
    }

    @Override
    public int getUnlockLevel() {
        return 1;
    }
}
