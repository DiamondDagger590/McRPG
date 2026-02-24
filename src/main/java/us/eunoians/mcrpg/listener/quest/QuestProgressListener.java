package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

/**
 * Base interface for listeners that drive quest objective progress from in-game events.
 * <p>
 * Concrete implementations listen for specific Bukkit events (block break, mob kill, etc.),
 * construct a {@link QuestObjectiveProgressContext}, and call {@link #progressQuests} to
 * apply progress to all matching active objectives for the contributing player.
 * <p>
 * This follows the same pattern as {@link us.eunoians.mcrpg.listener.ability.AbilityListener},
 * where the interface provides reusable default methods that concrete listeners delegate to.
 */
public interface QuestProgressListener extends Listener {

    /**
     * Iterates all active quests for the given player, finds objectives whose type can process the
     * provided context, and calls {@link QuestObjectiveInstance#progress} with the computed delta.
     *
     * @param playerUUID the UUID of the player whose action triggered this progress
     * @param context    the objective-type-specific progress context derived from the Bukkit event
     */
    default void progressQuests(@NotNull UUID playerUUID, @NotNull QuestObjectiveProgressContext context) {
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

        for (var questInstance : questManager.getActiveQuestsForPlayer(playerUUID)) {
            Optional<QuestDefinition> definition = questManager.getQuestDefinition(questInstance.getQuestKey());
            if (definition.isEmpty()) {
                continue;
            }

            for (QuestStageInstance stage : questInstance.getActiveQuestStages()) {
                for (QuestObjectiveInstance objective : stage.getQuestObjectives()) {
                    if (objective.getQuestObjectiveState() != QuestObjectiveState.IN_PROGRESS) {
                        continue;
                    }

                    definition.flatMap(def -> def.findObjectiveDefinition(objective.getQuestObjectiveKey()))
                            .ifPresent(objDef -> {
                                QuestObjectiveType type = objDef.getObjectiveType();
                                if (type.canProcess(context)) {
                                    long delta = type.processProgress(objective, context);
                                    if (delta > 0) {
                                        objective.progress(delta, playerUUID);
                                    }
                                }
                            });
                }
            }
        }
    }
}
