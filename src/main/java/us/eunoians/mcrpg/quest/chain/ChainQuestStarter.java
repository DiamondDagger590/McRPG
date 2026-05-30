package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Collaborator that encapsulates the repeated "resolve quest definition + resolve source +
 * call {@link QuestManager#startQuest}" pattern used at every chain step start site.
 * <p>
 * Eliminates four copies of the same resolution boilerplate across
 * {@link QuestChainManager#tryStartChain}, {@link QuestChainManager#advanceChain},
 * {@code applyReResolution}, and {@code startStepForPlayer}. Callers handle their own
 * chain-state mutation after a successful start (e.g. {@code state.advance()},
 * {@code state.resetToStep()}, or creating a new state).
 */
public class ChainQuestStarter {

    private final McRPG plugin;

    /**
     * Creates a new quest starter collaborator.
     *
     * @param plugin the McRPG plugin instance used to access registries
     */
    public ChainQuestStarter(@NotNull McRPG plugin) {
        this.plugin = plugin;
    }

    /**
     * Resolves the quest definition for {@code step} and the quest source from
     * {@code definition}, then calls {@link QuestManager#startQuest}. Returns
     * {@code true} if a {@link QuestInstance} was successfully created.
     * <p>
     * This method does <em>not</em> mutate any {@link QuestChainPlayerState} — callers are
     * responsible for updating state (e.g. {@code state.advance()}, {@code state.resetToStep()},
     * or rolling back an in-memory state insert) after inspecting the return value.
     *
     * @param playerUUID the UUID of the player for whom the quest should be started
     * @param definition the chain definition that owns this step
     * @param step       the step whose quest should be started
     * @return {@code true} if the quest was started successfully; {@code false} if the quest
     *         definition or source could not be resolved, or if {@link QuestManager#startQuest}
     *         returned empty
     */
    public boolean startStepQuest(@NotNull UUID playerUUID,
                                   @NotNull QuestChainDefinition definition,
                                   @NotNull QuestChainStep step) {
        NamespacedKey questKey = step.questKey();
        Optional<QuestDefinition> questDefOpt = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION).get(questKey);
        if (questDefOpt.isEmpty()) {
            plugin.getLogger().severe("[ChainQuestStarter] Chain '" + definition.getChainKey()
                    + "' step references unknown quest '" + questKey
                    + "' for player " + playerUUID);
            return false;
        }

        Optional<QuestSource> sourceOpt = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_SOURCE).get(definition.getSourceKey());
        if (sourceOpt.isEmpty()) {
            plugin.getLogger().severe("[ChainQuestStarter] Chain '" + definition.getChainKey()
                    + "' references unknown source '" + definition.getSourceKey()
                    + "' for player " + playerUUID);
            return false;
        }

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        Optional<QuestInstance> instance = questManager.startQuest(
                questDefOpt.get(), playerUUID, Map.of(), sourceOpt.get());
        if (instance.isEmpty()) {
            plugin.getLogger().warning("[ChainQuestStarter] Chain '" + definition.getChainKey()
                    + "' failed to start quest '" + questKey + "' for player " + playerUUID);
            return false;
        }
        return true;
    }
}
