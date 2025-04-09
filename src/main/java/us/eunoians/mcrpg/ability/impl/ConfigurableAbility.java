package us.eunoians.mcrpg.ability.impl;

import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.builder.item.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

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

    @NotNull
    Route getDisplayItemRoute();

    @NotNull
    @Override
    default String getName(@NotNull McRPGPlayer player) {
        return player.getPlugin().getLocalizationManager().getLocalizedMessage(player, Route.addTo(getDisplayItemRoute(), "ability-name"));
    }

    @NotNull
    @Override
    default AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        return AbilityItemBuilder.from(player.getPlugin().getLocalizationManager().getLocalizedSection(player, getDisplayItemRoute()), player, this);
    }

    @NotNull
    @Override
    default String getDisplayName(@NotNull McRPGPlayer player) {
        return player.getPlugin().getLocalizationManager().getLocalizedMessage(player, Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME));
    }
}
