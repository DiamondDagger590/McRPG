package us.eunoians.mcrpg.builder.item.skill;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.Skill;

public class SkillItemBuilder extends ItemBuilder {

    private final McRPGPlayer player;
    private final Skill skill;

    public SkillItemBuilder(@NotNull final ItemStack itemStack, @NotNull McRPGPlayer player, @NotNull final Skill skill) {
        super(itemStack);
        this.player = player;
        this.skill = skill;
        addPlaceholders();
    }

    public SkillItemBuilder(@NotNull final String value, @NotNull McRPGPlayer player, @NotNull final Skill skill) {
        super(value);
        this.player = player;
        this.skill = skill;
        addPlaceholders();
    }

    /**
     * Constructs a {@link SkillItemBuilder} by copying all builder state from an existing
     * {@link ItemBuilder} without baking to an {@link org.bukkit.inventory.ItemStack}. This
     * keeps lore and display name in string form so placeholders added here are resolved in
     * the same MiniMessage parse pass as palette tags.
     *
     * @param source The builder to copy state from.
     * @param player The player context for placeholder resolution.
     * @param skill  The skill this builder is for.
     */
    public SkillItemBuilder(@NotNull final ItemBuilder source, @NotNull McRPGPlayer player, @NotNull final Skill skill) {
        super(source);
        this.player = player;
        this.skill = skill;
        addPlaceholders();
    }

    @NotNull
    public static SkillItemBuilder from(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return new SkillItemBuilder(itemBuilder, mcRPGPlayer, skill);
    }

    @NotNull
    public static SkillItemBuilder from(@NotNull Section section, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return new SkillItemBuilder(ItemBuilder.from(section), mcRPGPlayer, skill);
    }

    private void addPlaceholders() {
        // Skill placeholder
        addPlaceholder(SkillItemPlaceholderKeys.SKILL.getKey(), skill.getName(player));
        var skillOptional = player.asSkillHolder().getSkillHolderData(skill);
        addPlaceholder(SkillItemPlaceholderKeys.LEVEL.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getCurrentLevel).orElse(0)));
        addPlaceholder(SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getCurrentExperience).orElse(0)));
        addPlaceholder(SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getExperienceForNextLevel).orElse(0)));
        addPlaceholder(SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getRemainingExperienceForNextLevel).orElse(0)));
    }
}
