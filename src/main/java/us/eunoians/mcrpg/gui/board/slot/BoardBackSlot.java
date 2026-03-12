package us.eunoians.mcrpg.gui.board.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * "Back" slot that navigates from the Quest Board GUI back to the Home GUI.
 */
public class BoardBackSlot implements McRPGSlot {

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        HomeGui homeGui = new HomeGui(mcRPGPlayer);
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(player, homeGui);
            player.openInventory(homeGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedSection(mcRPGPlayer, LocalizationKey.GUI_COMMON_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestBoardGui.class);
    }
}
