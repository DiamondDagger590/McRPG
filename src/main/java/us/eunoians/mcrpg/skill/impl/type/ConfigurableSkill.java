package us.eunoians.mcrpg.skill.impl.type;

import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.skill.SkillItemBuilder;
import us.eunoians.mcrpg.configuration.file.skill.SkillConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

public interface ConfigurableSkill extends Skill {

    /**
     * Gets the {@link YamlDocument} used to pull configuration data out of.
     *
     * @return The {@link YamlDocument} used to pull configuration data out of.
     */
    @NotNull
    YamlDocument getYamlDocument();

    /**
     * Gets the {@link Route} containing the {@link dev.dejvokep.boostedyaml.block.implementation.Section}
     * for the skill's display item.
     *
     * @return The {@link Route} containing the {@link dev.dejvokep.boostedyaml.block.implementation.Section}
     * for the skill's display item.
     */
    @NotNull
    Route getDisplayItemRoute();

    @NotNull
    @Override
    default String getName(@NotNull McRPGPlayer player) {
        return player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, Route.addTo(getDisplayItemRoute(), "skill-name"));
    }

    @NotNull
    @Override
    default String getName(){
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(Route.addTo(getDisplayItemRoute(), "skill-name"));
    }

    @NotNull
    @Override
    default SkillItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        return SkillItemBuilder.from(player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(player, getDisplayItemRoute()), player, this);
    }

    @NotNull
    @Override
    default String getDisplayName(@NotNull McRPGPlayer player) {
        return player.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(player, Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME));
    }

    @NotNull
    @Override
    default String getDisplayName() {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME));
    }

    @Override
    default int getMaxLevel() {
        return getYamlDocument().getInt(SkillConfigFile.MAXIMUM_SKILL_LEVEL);
    }

    @Override
    default boolean isSkillEnabled() {
        return getYamlDocument().getBoolean(SkillConfigFile.SKILL_ENABLED);
    }

    @NotNull
    @Override
    default Parser getLevelUpEquation() {
        return new Parser(getYamlDocument().getString(SkillConfigFile.LEVEL_UP_EQUATION));
    }
}
