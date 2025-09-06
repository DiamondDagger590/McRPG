package us.eunoians.mcrpg.ability.impl.type.configurable;

import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;

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

    /**
     * Gets the {@link Route} containing the {@link dev.dejvokep.boostedyaml.block.implementation.Section}
     * for the ability's display item.
     *
     * @return The {@link Route} containing the {@link dev.dejvokep.boostedyaml.block.implementation.Section}
     * for the ability's display item.
     */
    @NotNull
    Route getDisplayItemRoute();

    /**
     * Gets the {@link Route} used to check if this ability is enabled or not.
     *
     * @return The {@link Route} used to check if this ability is enabled or not.
     */
    @NotNull
    Route getAbilityEnabledRoute();

    @NotNull
    @Override
    default String getName(@NotNull McRPGPlayer player) {
        return player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, Route.addTo(getDisplayItemRoute(), "ability-name"));
    }

    @NotNull
    @Override
    default String getName() {
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(Route.addTo(getDisplayItemRoute(), "ability-name"));
    }

    @NotNull
    @Override
    default AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        return AbilityItemBuilder.from(player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(player, getDisplayItemRoute()), player, this);
    }

    @NotNull
    @Override
    default Component getDisplayName(@NotNull McRPGPlayer player) {
        return player.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(player, Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME), Map.of("ability", getName(player)));
    }

    @NotNull
    @Override
    default Component getDisplayName() {
        return RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME), Map.of("ability", getName()));
    }

    @Override
    default boolean isAbilityEnabled() {
        return getYamlDocument().getBoolean(getAbilityEnabledRoute());
    }
}
