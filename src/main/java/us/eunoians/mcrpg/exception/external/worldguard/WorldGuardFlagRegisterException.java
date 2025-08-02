package us.eunoians.mcrpg.exception.external.worldguard;

import org.jetbrains.annotations.NotNull;

/**
 * This exception gets thrown whenever a {@link com.sk89q.worldguard.protection.flags.StateFlag} is registered
 * for McRPG, but is conflicting with a flag registered by another plugin.
 */
public class WorldGuardFlagRegisterException extends RuntimeException {

    private final String stateFlagKey;

    public WorldGuardFlagRegisterException(@NotNull String stateFlagKey) {
        super(String.format("Unable to register World Guard flag %s as there is some sort of plugin conflict... this is bad... let the developer know", stateFlagKey));
        this.stateFlagKey = stateFlagKey;
    }

    public WorldGuardFlagRegisterException(@NotNull String stateFlagKey, @NotNull String message) {
        super(message);
        this.stateFlagKey = stateFlagKey;
    }

    /**
     * Gets the key of the {@link com.sk89q.worldguard.protection.flags.StateFlag} that caused this exception.
     *
     * @return The key of the {@link com.sk89q.worldguard.protection.flags.StateFlag} that caused this exception.
     */
    @NotNull
    public String getStateFlagKey() {
        return stateFlagKey;
    }
}
