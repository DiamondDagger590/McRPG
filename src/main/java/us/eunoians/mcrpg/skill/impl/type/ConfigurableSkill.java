package us.eunoians.mcrpg.skill.impl.type;

import com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.builder.item.skill.SkillItemBuilder;
import us.eunoians.mcrpg.builder.item.skill.SkillItemPlaceholderKeys;
import us.eunoians.mcrpg.configuration.file.skill.SkillConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Map;

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

    /**
     * Resolves the skill name from the locale {@code name:} field with palette color tags applied.
     * <p>
     * The {@code name:} field contains self-closing skill palette tags (e.g. {@code <skill-swords><skill></skill-swords>}).
     * These are resolved by {@code postProcessResolvedString} during {@code getLocalizedMessage}, producing
     * a raw MiniMessage string (e.g. {@code <color:#C75050>Swords</color:#C75050>}) that is safe
     * to embed in any template without double-coloring.
     *
     * @param player The player whose locale chain is used for resolution.
     * @return The colored skill name as a raw MiniMessage string.
     */
    @NotNull
    @Override
    default String getColoredName(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localizationManager = player.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        return localizationManager.getLocalizedMessage(
                player,
                Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME),
                Map.of(SkillItemPlaceholderKeys.SKILL.getKey(), getName(player)));
    }

    @NotNull
    @Override
    default String getName(){
        return McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(Route.addTo(getDisplayItemRoute(), "skill-name"));
    }

    @NotNull
    @Override
    default SkillItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
        McRPGLocalizationManager localizationManager = player.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        ItemBuilder intermediate = ItemBuilder.from(
                localizationManager.getLocalizedSection(player, getDisplayItemRoute()));
        intermediate.applyTagReplacements(localizationManager.getPaletteReplacements());
        return new SkillItemBuilder(intermediate, player, this);
    }

    @NotNull
    @Override
    default Component getDisplayName(@NotNull McRPGPlayer player) {
        return player.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(player, Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME), Map.of("skill", getName(player)));
    }

    @NotNull
    @Override
    default Component getDisplayName() {
        return RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(Route.addTo(getDisplayItemRoute(), ItemBuilderConfigurationKeys.NAME), Map.of("skill", getName()));
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
