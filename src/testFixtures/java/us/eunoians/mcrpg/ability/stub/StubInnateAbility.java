package us.eunoians.mcrpg.ability.stub;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;

/**
 * Stub innate {@link PassiveAbility} with no unlock gate for tests.
 */
public final class StubInnateAbility extends StubAbilityBase implements PassiveAbility {

    /**
     * @param plugin the McRPG plugin instance
     * @param name   the ability key name
     */
    public StubInnateAbility(@NotNull McRPG plugin, @NotNull String name) {
        super(plugin, name);
    }
}
