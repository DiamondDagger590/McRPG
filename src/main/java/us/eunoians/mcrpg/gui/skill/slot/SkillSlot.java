package us.eunoians.mcrpg.gui.skill.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.skill.SkillGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Set;

/**
 * This slot is used in {@link SkillGui}s to represent a {@link Skill}
 * while providing click actions for said skill.
 */
public class SkillSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Skill skill;

    public SkillSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.skill = skill;
    }

    /**
     * Gets the {@link McRPGPlayer} creating this slot.
     *
     * @return The {@link McRPGPlayer} creating this slot.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    /**
     * Gets the {@link Skill} represented by this slot.
     *
     * @return The {@link Skill} represented by this slot.
     */
    @NotNull
    public Skill getSkill() {
        return skill;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return skill.getDisplayItemBuilder(mcRPGPlayer);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(SkillGui.class);
    }
}