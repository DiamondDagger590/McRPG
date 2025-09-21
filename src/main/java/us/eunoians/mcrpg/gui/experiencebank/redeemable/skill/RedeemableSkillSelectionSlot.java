package us.eunoians.mcrpg.gui.experiencebank.redeemable.skill;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Set;

public class RedeemableSkillSelectionSlot implements McRPGSlot {

    private final Skill skill;

    public RedeemableSkillSelectionSlot(@NotNull Skill skill){
        this.skill = skill;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer corePlayer) {
        return McRPGSlot.super.getItem(corePlayer);
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer corePlayer, @NotNull ClickType clickType) {
        return false;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(RedeemableSkillSelectionGui.class);
    }
}
