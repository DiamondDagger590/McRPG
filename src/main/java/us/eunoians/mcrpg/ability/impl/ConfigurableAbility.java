package us.eunoians.mcrpg.ability.impl;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;

/**
 * This interface represents an {@link Ability} that has configuration that comes out of
 * a config file.
 */
public interface ConfigurableAbility extends Ability {

    /**
     * Gets the {@link YamlDocument} used to pull configuration data out of.
     *
     * @return The {@link YamlDocument} used to pull configuration data out of.
     */
    @NotNull
    YamlDocument getYamlDocument();
}
