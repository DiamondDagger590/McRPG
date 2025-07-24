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

    @NotNull
    public static SkillItemBuilder from(@NotNull ItemBuilder itemBuilder, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return new SkillItemBuilder(itemBuilder.asItemStack(), mcRPGPlayer, skill);
    }

    @NotNull
    public static SkillItemBuilder from(@NotNull Section section, @NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return new SkillItemBuilder(ItemBuilder.from(section).asItemStack(), mcRPGPlayer, skill);
    }

    private void addPlaceholders() {
        // Skill placeholder
        addPlaceholder(SkillItemPlaceholderKeys.SKILL.getKey(), skill.getName(player));
        var skillOptional = player.asSkillHolder().getSkillHolderData(skill);
        addPlaceholder(SkillItemPlaceholderKeys.LEVEL.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getCurrentLevel).orElse(0)));
        addPlaceholder(SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getCurrentExperience).orElse(0)));
        addPlaceholder(SkillItemPlaceholderKeys.EXPERIENCE_TO_LEVEL_UP.getKey(), Integer.toString(skillOptional.map(SkillHolder.SkillHolderData::getExperienceForNextLevel).orElse(0)));
    }
}
