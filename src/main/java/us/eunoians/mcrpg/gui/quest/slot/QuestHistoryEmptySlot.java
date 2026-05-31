package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestHistoryGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * Info slot placed at the center of {@link QuestHistoryGui} when the player has no
 * completed quests or chain runs to display. Shows a localized "no completed quests" message.
 */
public class QuestHistoryEmptySlot implements McRPGSlot {

    /**
     * Builds a gray-dye info item whose display name is the localized empty-state message.
     *
     * @param mcRPGPlayer the player viewing the slot; used for locale resolution
     * @return the item builder for the empty-state indicator
     */
    @Override
    @NotNull
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        String emptyMessage = localizationManager.getLocalizedMessage(
                mcRPGPlayer, LocalizationKey.QUEST_HISTORY_GUI_EMPTY_STATE);
        String resolvedName = localizationManager.resolvePaletteColors(emptyMessage);
        return ItemBuilder.from(new ItemStack(Material.GRAY_DYE))
                .setDisplayName(resolvedName);
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @Override
    @NotNull
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestHistoryGui.class);
    }
}
