package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Set;

/**
 * Fixed slot in the quest detail GUI showing how long the quest lasts before expiry.
 */
public class QuestDetailDurationSlot implements McRPGSlot {

    @Nullable
    private final QuestDefinition definition;

    public QuestDetailDurationSlot(@Nullable QuestDefinition definition) {
        this.definition = definition;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        String duration = definition != null
                ? definition.getExpiration()
                .map(expiration -> McRPGMethods.formatDuration(expiration.toMillis()))
                .orElse(localization.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE))
                : localization.getLocalizedMessage(mcRPGPlayer, LocalizationKey.ACTIVE_QUEST_GUI_EXPIRES_NONE);

        return ItemBuilder.from(localization.getLocalizedSection(
                        mcRPGPlayer,
                        LocalizationKey.QUEST_DETAIL_GUI_DURATION_SLOT_DISPLAY_ITEM))
                .addPlaceholders(Map.of("quest_duration", duration));
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
