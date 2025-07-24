package us.eunoians.mcrpg.skill;

import com.diamonddagger590.mccore.parser.Parser;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.builder.item.skill.SkillItemBuilder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Map;

/**
 * The base interface for all skills, providing basic behavior outlines that gain some definition
 * in {@link BaseSkill}.
 * <p>
 * Further skill behavior is provided in child interfaces which can be implemented alongside
 * extending {@link BaseSkill} in order to provide more out-of-the-box behavior.
 */
public interface Skill extends McRPGContent {

    /**
     * Gets the {@link Plugin} that owns this ability.
     *
     * @return The {@link Plugin} that owns this ability.
     */
    @NotNull
    Plugin getPlugin();

    /**
     * Gets the {@link NamespacedKey} that represents this skill
     *
     * @return The {@link NamespacedKey} that represents this skill
     */
    @NotNull NamespacedKey getSkillKey();

    /**
     * Gets the database name for a skill. This is an internal
     * use-only name used for database storage.
     *
     * @return The database name for a skill. This is an internal
     * use-only name used for database storage.
     */
    @NotNull
    String getDatabaseName();

    /**
     * Gets the localized name of the skill for the provided {@link McRPGPlayer}.
     *
     * @param player The player whose localization to use.
     * @return The localized name of the skill.
     */
    @NotNull
    String getName(@NotNull McRPGPlayer player);

    /**
     * Gets the localized name of the skill using the default locale.
     *
     * @return The localized name of the skill.
     */
    @NotNull
    String getName();

    /**
     * Gets the name to display in messages or guis for this skill. This may have a placeholder
     * such as {@code <skill-name>} which should be replaced by {@link #getName()}.
     *
     * @param player The {@link McRPGPlayer} to get the localized display name for.
     * @return The name to display in messages or guis for this skill.
     */
    @NotNull
    String getDisplayName(@NotNull McRPGPlayer player);

    /**
     * Gets the name to display in messages or guis for this skill using the default locale.
     * This may have a placeholder such as {@code <skill-name>} which should be replaced by {@link #getName()}.
     *
     * @return The name to display in messages or guis for this skill.
     */
    @NotNull
    String getDisplayName();

    /**
     * Gets the maximum level that this skill can be leveled to.
     *
     * @return The maximum level that this skill can be leveled to.
     */
    int getMaxLevel();

    /**
     * Checks to see if this skill is enabled.
     *
     * @return {@code true} if this skill is enabled.
     */
    boolean isSkillEnabled();

    /**
     * Gets the {@link SkillItemBuilder} for this skill based off the provided
     * {@link McRPGPlayer}.
     *
     * @param player The {@link McRPGPlayer} to get an item builder for.
     * @return The {@link SkillItemBuilder} for this skill based off the provided
     * {@link McRPGPlayer}.
     */
    @NotNull
    SkillItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player);

    /**
     * Gets a map containing the placeholders supported for this skill using the given
     * {@link McRPGPlayer}.
     * <p>
     * The key will be the placeholder itself whilst the value will be the string to replace the
     * placeholder with. Placeholders should follow the format of {@code <example>}.
     * <p>
     * Some generic placeholders are provided out of box in the {@link SkillItemBuilder}
     * itself,
     *
     * @param player The player to build the placeholders for.
     * @return A map containing the placeholders to use for this skill display.
     */
    @NotNull
    default Map<String, String> getItemBuilderPlaceholders(@NotNull McRPGPlayer player) {
        return Map.of();
    }

    @NotNull
    Parser getLevelUpEquation();
}
