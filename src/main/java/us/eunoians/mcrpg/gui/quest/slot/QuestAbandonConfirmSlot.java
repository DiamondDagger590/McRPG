package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestAbandonConfirmGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Confirm button in the {@link QuestAbandonConfirmGui}.
 * Calls {@link QuestManager#abandonQuest} and sends localized feedback.
 */
public class QuestAbandonConfirmSlot implements McRPGSlot {

    private final QuestInstance questInstance;
    private final String questDisplayName;

    public QuestAbandonConfirmSlot(@NotNull QuestInstance questInstance, @NotNull String questDisplayName) {
        this.questInstance = questInstance;
        this.questDisplayName = questDisplayName;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            QuestManager questManager = McRPG.getInstance().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.QUEST);

            boolean success = questManager.abandonQuest(questInstance.getQuestUUID());
            if (success) {
                player.sendMessage(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.QUEST_BOARD_ABANDONED,
                                Map.of("quest_name", questDisplayName)));
            }
            player.closeInventory();
        });
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
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_ABANDON_CONFIRM_GUI_CONFIRM_BUTTON_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestAbandonConfirmGui.class);
    }
}
