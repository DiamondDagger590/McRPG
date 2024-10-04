package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Optional;

/**
 * An ability that represents any abilities that come natively with McRPG.
 */
public abstract class McRPGAbility extends BaseAbility {

    public McRPGAbility(@NotNull McRPG mcRPG, @NotNull NamespacedKey namespacedKey) {
        super(mcRPG, namespacedKey);
    }

    @Override
    @NotNull
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
