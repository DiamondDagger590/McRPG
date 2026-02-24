package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Phase header slot in the {@link QuestDetailGui}, showing phase number and completion mode.
 */
public class QuestDetailPhaseSlot implements McRPGSlot {

    private final QuestPhaseDefinition phaseDef;
    private final int totalPhases;

    public QuestDetailPhaseSlot(@NotNull QuestPhaseDefinition phaseDef, int totalPhases) {
        this.phaseDef = phaseDef;
        this.totalPhases = totalPhases;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("phase_number", String.valueOf(phaseDef.getPhaseIndex() + 1));
        placeholders.put("phase_total", String.valueOf(totalPhases));
        placeholders.put("completion_mode", phaseDef.getCompletionMode().name());

        return ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_PHASE_HEADER_DISPLAY_ITEM))
                .addPlaceholders(placeholders);
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
