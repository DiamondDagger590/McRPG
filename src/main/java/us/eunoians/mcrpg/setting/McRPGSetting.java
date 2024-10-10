package us.eunoians.mcrpg.setting;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Optional;

/**
 * This setting represents a setting that belongs to the {@link McRPGExpansion}.
 */
public interface McRPGSetting extends PlayerSetting {

    @NotNull
    @Override
    default Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
