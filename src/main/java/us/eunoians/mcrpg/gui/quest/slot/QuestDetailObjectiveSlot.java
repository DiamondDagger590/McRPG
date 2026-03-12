package us.eunoians.mcrpg.gui.quest.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.quest.QuestDetailGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Objective slot in the {@link QuestDetailGui}, showing objective description and progress.
 */
public class QuestDetailObjectiveSlot implements McRPGSlot {

    private final NamespacedKey questKey;
    private final QuestObjectiveDefinition objectiveDef;
    @Nullable
    private final QuestObjectiveInstance objectiveInstance;

    public QuestDetailObjectiveSlot(@NotNull NamespacedKey questKey,
                                    @NotNull QuestObjectiveDefinition objectiveDef,
                                    @Nullable QuestObjectiveInstance objectiveInstance) {
        this.questKey = questKey;
        this.objectiveDef = objectiveDef;
        this.objectiveInstance = objectiveInstance;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        Map<String, String> placeholders = new HashMap<>();

        String description = objectiveDef.getDescription(mcRPGPlayer, questKey);
        String[] descLines = description.split("\n");
        placeholders.put("objective_description", descLines[0]);

        String progress;
        String required;
        String state;

        if (objectiveInstance != null) {
            progress = String.valueOf(objectiveInstance.getCurrentProgression());
            required = String.valueOf(objectiveInstance.getRequiredProgression());
            state = objectiveInstance.getQuestObjectiveState().name();
        } else {
            progress = "0";
            try {
                required = String.valueOf(objectiveDef.getRequiredProgress());
            } catch (IllegalStateException e) {
                required = "?";
            }
            state = "PREVIEW";
        }

        ItemBuilder builder = ItemBuilder.from(RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_DETAIL_GUI_OBJECTIVE_SLOT_DISPLAY_ITEM))
                .addPlaceholders(placeholders);

        for (int i = 1; i < descLines.length; i++) {
            builder.addDisplayLore("<gray>" + descLines[i]);
        }

        builder.addDisplayLore("<gray>Progress: <gold>" + progress + "/" + required);
        builder.addDisplayLore("<gray>State: <gold>" + state);

        return builder;
    }

    @NotNull
    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(QuestDetailGui.class);
    }
}
