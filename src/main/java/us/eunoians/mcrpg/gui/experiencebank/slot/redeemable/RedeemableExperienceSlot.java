package us.eunoians.mcrpg.gui.experiencebank.slot.redeemable;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.ExperienceBankGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;

import java.util.Set;

public class RedeemableExperienceSlot implements McRPGSlot {

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer corePlayer) {
        return McRPGSlot.super.getItem(corePlayer);
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer corePlayer, @NotNull ClickType clickType) {
        return true;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(ExperienceBankGui.class);
    }
}
