package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestAbandonConfirmGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Informational slot in the {@link QuestAbandonConfirmGui} showing the quest name.
 */
public class QuestAbandonInfoSlot implements McRPGSlot {

    private final String questDisplayName;

    public QuestAbandonInfoSlot(@NotNull String questDisplayName) {
        this.questDisplayName = questDisplayName;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("quest_name", questDisplayName);
        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_ABANDON_CONFIRM_GUI_QUEST_INFO_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestAbandonConfirmGui.class);
    }
}
